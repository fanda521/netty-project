package com.example.study.imclient.service;


import com.example.study.imclient.handler.NettyClientInitializer;
import com.example.study.imclient.model.ChatMessage;
import com.example.study.imclient.model.MessageType;
import com.example.study.imclient.util.JsonUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

/**
 * IM客户端服务（管理连接、发送消息）
 * @author 编程导师
 */
@Slf4j
@Service
public class ImClientService {
    /** Netty客户端线程组 */
    private EventLoopGroup group;

    /** 与服务端的通信通道 */
    private Channel channel;

    /** 服务端IP */
    @Value("${im.server.ip}")
    private String serverIp;

    /** 服务端Netty端口 */
    @Value("${im.server.port}")
    private int serverPort;

    /** 当前客户端用户ID（新增@Getter，供Controller调用） */
    @Getter
    @Value("${im.client.userId}")
    private String userId;

    @Autowired
    private NettyClientInitializer nettyClientInitializer;

    /**
     * 启动客户端并连接服务端（Spring初始化后执行，非阻塞）
     */
    @PostConstruct
    public void connect() {
        // 异步启动Netty客户端，避免阻塞Web容器
        new Thread(() -> {
            group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(nettyClientInitializer);

                // 连接服务端
                ChannelFuture future = bootstrap.connect(serverIp, serverPort).sync();
                channel = future.channel();
                log.info("IM客户端Netty连接成功，用户ID：{}，连接服务端：{}:{}", userId, serverIp, serverPort);

                // 等待通道关闭（仅阻塞当前线程，不影响Web容器）
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("IM客户端连接服务端失败", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 关闭客户端连接
     */
    @PreDestroy
    public void close() {
        if (group != null) {
            group.shutdownGracefully();
        }
        log.info("IM客户端已关闭");
    }

    /**
     * 发送消息到服务端
     * @param chatMessage 消息对象
     */
    public void sendMessage(ChatMessage chatMessage) {
        if (channel == null || !channel.isActive()) {
            log.error("客户端未连接到服务端，无法发送消息");
            throw new RuntimeException("客户端未连接到服务端");
        }

        // 补充消息基础信息
        if (chatMessage.getSenderId() == null) {
            chatMessage.setSenderId(userId);
        }
        if (chatMessage.getMsgId() == null) {
            chatMessage.setMsgId(UUID.randomUUID().toString().replace("-", ""));
        }

        // 序列化并发送
        String msgJson = JsonUtil.toJson(chatMessage);
        channel.writeAndFlush(msgJson);
        log.info("客户端发送消息：{}", msgJson);
    }

    /**
     * 加入聊天室
     * @param roomId 聊天室ID
     */
    public void joinChatRoom(String roomId) {
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(userId)
                .msgType(MessageType.JOIN_ROOM.getCode())
                .build();
        sendMessage(message);
    }

    /**
     * 退出聊天室
     * @param roomId 聊天室ID
     */
    public void quitChatRoom(String roomId) {
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(userId)
                .msgType(MessageType.QUIT_ROOM.getCode())
                .build();
        sendMessage(message);
    }

    /**
     * 发送普通聊天消息
     * @param roomId 聊天室ID
     * @param content 消息内容
     */
    public void sendChatMessage(String roomId, String content) {
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(userId)
                .content(content)
                .msgType(MessageType.CHAT_MESSAGE.getCode())
                .build();
        sendMessage(message);
    }
}