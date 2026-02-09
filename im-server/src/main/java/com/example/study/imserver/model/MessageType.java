package com.example.study.imserver.model;

import lombok.Getter;

/**
 * 消息类型枚举
 * @author 编程导师
 */
@Getter
public enum MessageType {
    /** 创建聊天室 */
    CREATE_ROOM(1, "创建聊天室"),
    /** 加入聊天室 */
    JOIN_ROOM(2, "加入聊天室"),
    /** 退出聊天室 */
    QUIT_ROOM(3, "退出聊天室"),
    /** 解散聊天室 */
    DISSOLVE_ROOM(4, "解散聊天室"),
    /** 普通聊天消息 */
    CHAT_MESSAGE(5, "普通聊天消息"),
    /** 系统通知 */
    SYSTEM_NOTICE(6, "系统通知");

    private final int code;
    private final String desc;

    MessageType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据编码获取枚举
    public static MessageType getByCode(int code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}