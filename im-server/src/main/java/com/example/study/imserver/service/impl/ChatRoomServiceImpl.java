package com.example.study.imserver.service.impl;


import com.alibaba.fastjson.JSON;
import com.example.study.imserver.model.ChatMessage;
import com.example.study.imserver.model.ChatRoom;
import com.example.study.imserver.model.MessageType;
import com.example.study.imserver.service.ChatRoomService;
import com.example.study.imserver.util.ChannelManager;
import com.example.study.imserver.util.JsonUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 聊天室业务实现类（基于Redis实现分布式状态共享）
 * @author 编程导师
 */
@Slf4j
@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    // Redis Key前缀
    private static final String REDIS_KEY_ROOM = "im:chat_room:"; // 聊天室基本信息（Hash）
    private static final String REDIS_KEY_ROOM_USERS = "im:chat_room_users:"; // 聊天室用户（Set）
    private static final String REDIS_CHANNEL_MSG = "im:chat_channel"; // 消息广播通道

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ChannelManager channelManager;

    /** 分布式节点ID */
    @Value("${im.node.id}")
    private String nodeId;

    @Override
    public ChatRoom createChatRoom(String roomName, String creatorId) {
        // 1. 生成唯一聊天室ID
        String roomId = UUID.randomUUID().toString().replace("-", "");

        // 2. 构建聊天室对象
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .roomName(roomName)
                .creatorId(creatorId)
                .createTime(new Date())
                .dissolved(false)
                .updateTime(new Date())
                .build();

        // 3. 存入Redis（永不过期，除非解散）
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(REDIS_KEY_ROOM + roomId, chatRoom);

        // 4. 添加创建人到聊天室用户列表
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.add(REDIS_KEY_ROOM_USERS + roomId, creatorId);

        log.info("创建聊天室成功：{}，创建人：{}", roomId, creatorId);
        return chatRoom;
    }

    @Override
    public boolean joinChatRoom(String roomId, String userId) {
        // 1. 校验聊天室是否存在且未解散
        ChatRoom chatRoom = getChatRoomById(roomId);
        if (chatRoom == null || chatRoom.isDissolved()) {
            log.warn("聊天室不存在或已解散：{}", roomId);
            return false;
        }

        // 2. 添加用户到聊天室用户列表
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        long result = setOps.add(REDIS_KEY_ROOM_USERS + roomId, userId);

        if (result > 0) {
            log.info("用户{}加入聊天室{}成功", userId, roomId);

            // 3. 发送系统通知
            ChatMessage notice = ChatMessage.builder()
                    .msgId(UUID.randomUUID().toString().replace("-", ""))
                    .roomId(roomId)
                    .senderId(userId)
                    .content("用户" + userId + "加入聊天室")
                    .msgType(MessageType.SYSTEM_NOTICE.getCode())
                    .sendTime(new Date())
                    .nodeId(nodeId)
                    .build();
            sendMessage(notice);
            return true;
        }

        log.warn("用户{}已在聊天室{}中", userId, roomId);
        return false;
    }

    @Override
    public boolean quitChatRoom(String roomId, String userId) {
        // 1. 校验聊天室是否存在
        ChatRoom chatRoom = getChatRoomById(roomId);
        if (chatRoom == null || chatRoom.isDissolved()) {
            log.warn("聊天室不存在或已解散：{}", roomId);
            return false;
        }

        // 2. 移除用户从聊天室用户列表
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        long result = setOps.remove(REDIS_KEY_ROOM_USERS + roomId, userId);

        if (result > 0) {
            log.info("用户{}退出聊天室{}成功", userId, roomId);

            // 3. 发送系统通知
            ChatMessage notice = ChatMessage.builder()
                    .msgId(UUID.randomUUID().toString().replace("-", ""))
                    .roomId(roomId)
                    .senderId("system")
                    .content("用户" + userId + "退出聊天室")
                    .msgType(MessageType.SYSTEM_NOTICE.getCode())
                    .sendTime(new Date())
                    .nodeId(nodeId)
                    .build();
            sendMessage(notice);
            return true;
        }

        log.warn("用户{}不在聊天室{}中", userId, roomId);
        return false;
    }

    @Override
    public boolean dissolveChatRoom(String roomId, String creatorId) {
        // 1. 校验聊天室是否存在
        ChatRoom chatRoom = getChatRoomById(roomId);
        if (chatRoom == null || chatRoom.isDissolved()) {
            log.warn("聊天室不存在或已解散：{}", roomId);
            return false;
        }

        // 2. 校验是否为创建人
        if (!chatRoom.getCreatorId().equals(creatorId)) {
            log.warn("用户{}不是聊天室{}的创建人，无法解散", creatorId, roomId);
            return false;
        }

        // 3. 更新聊天室状态为已解散
        chatRoom.setDissolved(true);
        chatRoom.setUpdateTime(new Date());
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(REDIS_KEY_ROOM + roomId, chatRoom);

        // 4. 发送系统通知
        ChatMessage notice = ChatMessage.builder()
                .msgId(UUID.randomUUID().toString().replace("-", ""))
                .roomId(roomId)
                .senderId("system")
                .content("聊天室" + chatRoom.getRoomName() + "已被创建人" + creatorId + "解散")
                .msgType(MessageType.SYSTEM_NOTICE.getCode())
                .sendTime(new Date())
                .nodeId(nodeId)
                .build();
        sendMessage(notice);

        // 5. （可选）设置聊天室信息过期时间（7天）
        redisTemplate.expire(REDIS_KEY_ROOM + roomId, 7, TimeUnit.DAYS);
        redisTemplate.expire(REDIS_KEY_ROOM_USERS + roomId, 7, TimeUnit.DAYS);

        log.info("聊天室{}已被创建人{}解散", roomId, creatorId);
        return true;
    }

    @Override
    public boolean sendMessage(ChatMessage chatMessage) {
        // 1. 校验聊天室是否存在且未解散
        String roomId = chatMessage.getRoomId();
        ChatRoom chatRoom = getChatRoomById(roomId);
        if (chatRoom == null || chatRoom.isDissolved()) {
            log.warn("聊天室不存在或已解散：{}", roomId);
            return false;
        }

        // 2. 补充消息默认值
        if (chatMessage.getMsgId() == null) {
            chatMessage.setMsgId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (chatMessage.getSendTime() == null) {
            chatMessage.setSendTime(new Date());
        }
        if (chatMessage.getNodeId() == null) {
            chatMessage.setNodeId(nodeId);
        }

        // 3. 推送消息给当前节点的该聊天室用户（精准推送）
        String msgJson = JsonUtil.toJson(chatMessage);
        Set<Object> userIds = redisTemplate.opsForSet().members(REDIS_KEY_ROOM_USERS + roomId);
        if (userIds != null && !userIds.isEmpty()) {
            for (Object userIdObj : userIds) {
                String userId = userIdObj.toString();
                Channel channel = channelManager.getChannelByUserId(userId);
                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(msgJson);
                    log.info("本地消息推送给聊天室{}的用户{}：{}", roomId, userId, msgJson);
                }
            }
        }

        // 4. 发布消息到Redis通道（供其他节点消费）
        stringRedisTemplate.convertAndSend(REDIS_CHANNEL_MSG, msgJson);
        log.info("消息发布到Redis通道：{}", msgJson);

        return true;
    }

    /**
     * 根据聊天室ID查询聊天室信息
     * @param roomId 聊天室ID
     * @return 聊天室信息（null表示不存在）
     */
    private ChatRoom getChatRoomById(String roomId) {
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        return (ChatRoom) valueOps.get(REDIS_KEY_ROOM + roomId);
    }
}