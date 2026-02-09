package com.example.study.imclient.handler;


import com.example.study.imclient.model.ChatMessage;
import com.example.study.imclient.model.MessageType;
import com.example.study.imclient.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Netty客户端业务处理器（接收服务端消息）
 * @author 编程导师
 */
@Slf4j
@Component
public class ImClientHandler extends SimpleChannelInboundHandler<String> {

    @Getter
    @Value("${im.client.userId}")
    private String userId;

    /**
     * 连接服务端成功时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("成功连接到IM服务端：{}", ctx.channel().remoteAddress());
        ChatMessage chatMessage = ChatMessage.builder()
                .msgId(UUID.randomUUID().toString().replace("-", ""))
                .msgType(MessageType.SYSTEM_NOTICE.getCode())
                .content("客户连接成功[" + userId + "]")
                .senderId(userId)
                .sendTime(new Date())
                .build();
        String json = JsonUtil.toJson(chatMessage);
        ctx.writeAndFlush(json);
        super.channelActive(ctx);
    }

    /**
     * 接收服务端消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        try {
            // 反序列化为消息对象
            ChatMessage chatMessage = JsonUtil.fromJson(msg, ChatMessage.class);
            if (chatMessage == null) {
                log.error("接收的消息格式错误：{}", msg);
                return;
            }

            // 打印消息（实际业务可扩展UI展示）
            String sender = chatMessage.getSenderId().equals("system") ? "【系统通知】" : "【用户" + chatMessage.getSenderId() + "】";
            System.out.println("\n" + sender + " " + chatMessage.getSendTime() + "：" + chatMessage.getContent());
        } catch (Exception e) {
            log.error("解析服务端消息失败", e);
        }
    }

    /**
     * 断开连接时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与IM服务端断开连接");
        super.channelInactive(ctx);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("与服务端通信异常", cause);
        ctx.close();
    }
}