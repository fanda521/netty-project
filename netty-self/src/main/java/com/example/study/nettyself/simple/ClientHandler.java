package com.example.study.nettyself.simple;

import com.example.study.nettyself.protobuf.StudentPOJO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Netty客户端消息处理器
 * 处理服务端回复的消息
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 核心方法：接收并处理服务端回复的消息
     * @param ctx 通道上下文
     * @param msg 服务端回复的消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("收到服务端回复：" + msg);
    }

    /**
     * 处理异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("客户端处理消息时发生异常：" + cause.getMessage());
        ctx.close();
    }
}