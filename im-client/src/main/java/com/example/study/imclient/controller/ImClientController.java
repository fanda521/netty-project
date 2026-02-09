package com.example.study.imclient.controller;


import com.example.study.imclient.model.ChatMessage;
import com.example.study.imclient.model.MessageType;
import com.example.study.imclient.service.ImClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * IM客户端Controller层（HTTP接口调用客户端功能）
 * 接口功能：加入聊天室、退出聊天室、发送消息
 * @author 编程导师
 */
@Slf4j
@RestController
@RequestMapping("/im/client")
public class ImClientController {

    @Autowired
    private ImClientService imClientService;

    /**
     * 加入聊天室
     * @param roomId 聊天室ID
     * @return 操作结果
     */
    @PostMapping("/joinRoom")
    public String joinRoom(@RequestParam String roomId) {
        try {
            imClientService.joinChatRoom(roomId);
            return "成功加入聊天室：" + roomId;
        } catch (Exception e) {
            log.error("加入聊天室失败", e);
            return "加入聊天室失败：" + e.getMessage();
        }
    }

    /**
     * 退出聊天室
     * @param roomId 聊天室ID
     * @return 操作结果
     */
    @PostMapping("/quitRoom")
    public String quitRoom(@RequestParam String roomId) {
        try {
            imClientService.quitChatRoom(roomId);
            return "成功退出聊天室：" + roomId;
        } catch (Exception e) {
            log.error("退出聊天室失败", e);
            return "退出聊天室失败：" + e.getMessage();
        }
    }

    /**
     * 发送普通聊天消息
     * @param roomId 聊天室ID
     * @param content 消息内容
     * @return 操作结果
     */
    @PostMapping("/sendMessage")
    public String sendMessage(
            @RequestParam String roomId,
            @RequestParam String content) {
        try {
            imClientService.sendChatMessage(roomId, content);
            return "消息发送成功：" + content;
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return "发送消息失败：" + e.getMessage();
        }
    }

    /**
     * 自定义发送消息（支持指定消息类型，扩展用）
     * @param roomId 聊天室ID
     * @param content 消息内容
     * @param msgType 消息类型（参考MessageType枚举）
     * @return 操作结果
     */
    @PostMapping("/sendCustomMessage")
    public String sendCustomMessage(
            @RequestParam String roomId,
            @RequestParam String content,
            @RequestParam(defaultValue = "5") int msgType) {
        try {
            ChatMessage message = ChatMessage.builder()
                    .msgId(UUID.randomUUID().toString().replace("-", ""))
                    .roomId(roomId)
                    .content(content)
                    .msgType(msgType)
                    .build();
            imClientService.sendMessage(message);
            return "自定义消息发送成功，类型：" + MessageType.getByCode(msgType).getDesc();
        } catch (Exception e) {
            log.error("发送自定义消息失败", e);
            return "发送自定义消息失败：" + e.getMessage();
        }
    }
}