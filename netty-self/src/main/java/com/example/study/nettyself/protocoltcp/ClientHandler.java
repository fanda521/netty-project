package com.example.study.nettyself.protocoltcp;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/6 2:18
 */
public class ClientHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i < 10; i++) {
            String msg= "hello 杰克 " + i;
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            MessageProtocol messageProtocol = new MessageProtocol(bytes.length, bytes);
            ctx.writeAndFlush(messageProtocol);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol messageProtocol) throws Exception {
        int len = messageProtocol.getLen();
        byte[] bytes = messageProtocol.getContent();
        String message = new String(bytes, Charset.forName("utf-8"));
        System.out.println("服务器端接收到数据:长度:" + len + ",内容:" + message);
    }
}
