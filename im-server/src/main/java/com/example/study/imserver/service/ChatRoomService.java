package com.example.study.imserver.service;


import com.example.study.imserver.model.ChatMessage;
import com.example.study.imserver.model.ChatRoom;

/**
 * 聊天室业务接口
 * @author 编程导师
 */
public interface ChatRoomService {

    /**
     * 创建聊天室
     * @param roomName 聊天室名称
     * @param creatorId 创建人ID
     * @return 聊天室信息
     */
    ChatRoom createChatRoom(String roomName, String creatorId);

    /**
     * 加入聊天室
     * @param roomId 聊天室ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean joinChatRoom(String roomId, String userId);

    /**
     * 退出聊天室
     * @param roomId 聊天室ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean quitChatRoom(String roomId, String userId);

    /**
     * 解散聊天室（仅创建人可操作）
     * @param roomId 聊天室ID
     * @param creatorId 创建人ID
     * @return 是否成功
     */
    boolean dissolveChatRoom(String roomId, String creatorId);

    /**
     * 发送消息到聊天室
     * @param chatMessage 消息对象
     * @return 是否成功
     */
    boolean sendMessage(ChatMessage chatMessage);
}