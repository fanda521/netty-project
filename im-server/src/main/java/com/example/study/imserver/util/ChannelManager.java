package com.example.study.imserver.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty Channel管理器（维护用户ID与Channel的映射）
 * @author 编程导师
 */
@Component
public class ChannelManager {
    /** 全局Channel组（当前节点所有连接） */
    private static final ChannelGroup GLOBAL_CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /** 用户ID -> Channel映射（当前节点） */
    private static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    /** ChannelID -> 用户ID映射（当前节点） */
    private static final Map<ChannelId, String> CHANNEL_USER_MAP = new ConcurrentHashMap<>();

    /**
     * 添加Channel到管理器
     * @param userId 用户ID
     * @param channel 通道
     */
    public void addChannel(String userId, Channel channel) {
        GLOBAL_CHANNEL_GROUP.add(channel);
        USER_CHANNEL_MAP.put(userId, channel);
        CHANNEL_USER_MAP.put(channel.id(), userId);
    }

    /**
     * 移除Channel
     * @param channel 通道
     */
    public void removeChannel(Channel channel) {
        GLOBAL_CHANNEL_GROUP.remove(channel);
        String userId = CHANNEL_USER_MAP.remove(channel.id());
        if (userId != null) {
            USER_CHANNEL_MAP.remove(userId);
        }
    }

    /**
     * 根据用户ID获取Channel
     * @param userId 用户ID
     * @return 通道（null表示未连接）
     */
    public Channel getChannelByUserId(String userId) {
        return USER_CHANNEL_MAP.get(userId);
    }

    /**
     * 根据Channel获取用户ID
     * @param channel 通道
     * @return 用户ID
     */
    public String getUserIdByChannel(Channel channel) {
        return CHANNEL_USER_MAP.get(channel.id());
    }
}