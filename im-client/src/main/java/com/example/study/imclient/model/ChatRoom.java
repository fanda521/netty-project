package com.example.study.imclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天室实体类
 * @author 编程导师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 聊天室ID（唯一标识） */
    private String roomId;

    /** 聊天室名称 */
    private String roomName;

    /** 创建人ID */
    private String creatorId;

    /** 创建时间 */
    private Date createTime;

    /** 是否解散（true:已解散，false:正常） */
    private boolean dissolved;

    /** 最后更新时间 */
    private Date updateTime;
}