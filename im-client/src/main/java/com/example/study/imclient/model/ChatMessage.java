package com.example.study.imclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息实体类
 * @author 编程导师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private String msgId;

    /** 聊天室ID */
    private String roomId;

    /** 发送人ID */
    private String senderId;

    /** 消息内容 */
    private String content;

    /** 消息类型（参考MessageType枚举） */
    private int msgType;

    /** 发送时间 */
    private Date sendTime;

    /** 分布式节点ID（用于跨节点消息同步） */
    private String nodeId;
}