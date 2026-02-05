package com.example.study.nettyself.protocoltcp;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/6 2:24
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private int count = 0;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol msg) throws Exception {
        int len = msg.getLen();
        byte[] bytes = msg.getContent();
        String message = new String(bytes, Charset.forName("utf-8"));

        System.out.println("服务器端接收到数据:长度:" + len + ",内容:" + message + "，当前第" + (++count) + "条");


        //回复消息给客户端
        String resp = "服务端已收到数据,随机id" + UUID.randomUUID().toString();
        byte[] bytes1 = resp.getBytes(StandardCharsets.UTF_8);
        MessageProtocol messageProtocol = new MessageProtocol(bytes1.length, bytes1);
        channelHandlerContext.writeAndFlush(messageProtocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
