package com.example.study.imserver.handler;


import com.example.study.imserver.model.ChatMessage;
import com.example.study.imserver.util.ChannelManager;
import com.example.study.imserver.util.JsonUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Redis消息监听器（优化：仅推送给对应聊天室的在线用户）
 * @author 编程导师
 */
@Slf4j
@Component
public class RedisMessageListener implements MessageListener {

    // Redis聊天室用户列表的key前缀（和ChatRoomServiceImpl保持一致）
    private static final String REDIS_KEY_ROOM_USERS = "im:chat_room_users:";

    @Autowired
    private ChannelManager channelManager;

    @Autowired
    @Lazy
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 监听并消费Redis发布的消息（精准推送给聊天室在线用户）
     * @param message Redis发布的消息体（JSON格式的ChatMessage）
     * @param pattern 订阅的通道模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 1. 解析Redis消息为ChatMessage对象
            String msgJson = new String(message.getBody());
            ChatMessage chatMessage = JsonUtil.fromJson(msgJson, ChatMessage.class);
            if (chatMessage == null) {
                log.error("解析Redis消息失败：{}", msgJson);
                return;
            }

            // 2. 获取消息对应的聊天室ID（核心：精准定位聊天室）
            String roomId = chatMessage.getRoomId();
            String senderId = chatMessage.getSenderId();
            log.info("消费Redis跨实例消息：聊天室{}，发送人{}", roomId, senderId);

            // 3. 从Redis获取该聊天室的所有用户ID（仅处理该聊天室的用户）
            Set<Object> roomUserIds = redisTemplate.opsForSet().members(REDIS_KEY_ROOM_USERS + roomId);
            if (roomUserIds == null || roomUserIds.isEmpty()) {
                log.warn("聊天室{}暂无在线用户，无需推送跨实例消息", roomId);
                return;
            }

            // 4. 仅推送给当前实例中该聊天室的在线用户（精准推送）
            String finalMsgJson = JsonUtil.toJson(chatMessage);
            for (Object userIdObj : roomUserIds) {
                String userId = userIdObj.toString();
                // 跳过发送者自己（可选，根据业务需求）
                if (userId.equals(senderId)) {
                    continue;
                }
                // 获取当前实例中该用户的Channel
                Channel channel = channelManager.getChannelByUserId(userId);
                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(finalMsgJson);
                    log.info("跨实例消息推送给聊天室{}的本地用户{}", roomId, userId);
                }
            }
        } catch (Exception e) {
            log.error("处理Redis订阅消息失败", e);
        }
    }
}