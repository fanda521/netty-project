package com.example.study.imserver.controller;


import com.example.study.imserver.model.ChatMessage;
import com.example.study.imserver.model.ChatRoom;
import com.example.study.imserver.model.MessageType;
import com.example.study.imserver.service.ChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * 聊天室操作Controller（HTTP接口）
 * @author 编程导师
 */
@Slf4j
@RestController
@RequestMapping("/chatRoom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    /**
     * 创建聊天室
     * @param roomName 聊天室名称
     * @param creatorId 创建人ID
     * @return 聊天室信息
     */
    @PostMapping("/create")
    public ChatRoom createChatRoom(
            @RequestParam String roomName,
            @RequestParam String creatorId) {
        return chatRoomService.createChatRoom(roomName, creatorId);
    }

    /**
     * 加入聊天室
     * @param roomId 聊天室ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/join")
    public boolean joinChatRoom(
            @RequestParam String roomId,
            @RequestParam String userId) {
        return chatRoomService.joinChatRoom(roomId, userId);
    }

    /**
     * 退出聊天室
     * @param roomId 聊天室ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/quit")
    public boolean quitChatRoom(
            @RequestParam String roomId,
            @RequestParam String userId) {
        return chatRoomService.quitChatRoom(roomId, userId);
    }

    /**
     * 解散聊天室
     * @param roomId 聊天室ID
     * @param creatorId 创建人ID
     * @return 操作结果
     */
    @PostMapping("/dissolve")
    public boolean dissolveChatRoom(
            @RequestParam String roomId,
            @RequestParam String creatorId) {
        return chatRoomService.dissolveChatRoom(roomId, creatorId);
    }

    /**
     * 发送消息
     * @param roomId 聊天室ID
     * @param senderId 发送人ID
     * @param content 消息内容
     * @return 操作结果
     */
    @PostMapping("/sendMessage")
    public boolean sendMessage(
            @RequestParam String roomId,
            @RequestParam String senderId,
            @RequestParam String content) {
        ChatMessage chatMessage = ChatMessage.builder()
                .msgId(UUID.randomUUID().toString().replace("-", ""))
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .msgType(MessageType.CHAT_MESSAGE.getCode())
                .sendTime(new Date())
                .build();
        return chatRoomService.sendMessage(chatMessage);
    }
}