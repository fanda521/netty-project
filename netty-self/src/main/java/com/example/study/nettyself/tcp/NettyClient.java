package com.example.study.nettyself.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/6 2:03
 */
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ClientInitializer());

            System.out.println("客户端开始连接...");
            ChannelFuture future = bootstrap.connect("localhost", 8888).sync();
            future.addListener(future1 -> {
                if (future1.isSuccess()) {
                    System.out.println("客户端已启动成功！");
                } else {
                    System.out.println("客户端启动失败！");
                }
            });

            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            System.out.println("客户端优雅关闭");
        }


    }
}
