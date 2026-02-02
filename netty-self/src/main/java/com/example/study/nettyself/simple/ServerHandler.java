package com.example.study.nettyself.simple;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Netty服务端消息处理器
 * 继承SimpleChannelInboundHandler<String>：泛型指定处理的消息类型为String
 * SimpleChannelInboundHandler会自动释放消息资源，适合服务端接收消息的场景
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 当客户端连接建立成功后触发的方法
     * @param ctx 通道上下文：包含通道、处理器、事件循环等核心信息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientIp = ctx.channel().remoteAddress().toString();
        System.out.println("客户端[" + clientIp + "]已连接");
    }

    /**
     * 核心方法：接收并处理客户端发送的消息
     * @param ctx 通道上下文
     * @param msg 客户端发送的消息（已被StringDecoder解码为String）
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String clientIp = ctx.channel().remoteAddress().toString();
        System.out.println("收到客户端[" + clientIp + "]的消息：" + msg);

        // 服务端回复客户端（异步发送，不会阻塞当前线程）
        String response = "服务端已收到消息：" + msg + "，当前时间戳：" + System.currentTimeMillis();
        ctx.writeAndFlush(response); // write：写入缓冲区，flush：刷新缓冲区（发送消息）
    }

    /**
     * 当客户端连接断开后触发的方法
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientIp = ctx.channel().remoteAddress().toString();
        System.out.println("客户端[" + clientIp + "]已断开连接");
    }

    /**
     * 当处理消息过程中发生异常时触发的方法
     * @param ctx 通道上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("服务端处理消息时发生异常：" + cause.getMessage());
        // 关闭通道，释放资源
        ctx.close();
    }
}