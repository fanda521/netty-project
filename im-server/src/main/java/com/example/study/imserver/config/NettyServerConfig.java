package com.example.study.imserver.config;

import com.example.study.imserver.handler.NettyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Netty服务端配置（启动/关闭Netty服务）
 * @author 编程导师
 */
@Slf4j
@Configuration
public class NettyServerConfig {
    /** Boss线程组：处理客户端连接请求 */
    private EventLoopGroup bossGroup;

    /** Worker线程组：处理已连接客户端的IO操作 */
    private EventLoopGroup workerGroup;

    /** Netty服务端端口 */
    @Value("${netty.server.port}")
    private int nettyPort;

    @Autowired
    private NettyServerInitializer nettyServerInitializer;

    @PostConstruct
    public void start() {
        // 核心修改：新建异步线程启动Netty，释放Spring主线程
        new Thread(() -> {
            bossGroup = new NioEventLoopGroup(1); // 单线程处理连接请求
            workerGroup = new NioEventLoopGroup(); // 默认CPU核心数*2的线程数处理IO

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class) // NIO模式
                        .option(ChannelOption.SO_BACKLOG, 1024) // 连接队列大小
                        .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持长连接
                        .childHandler(nettyServerInitializer); // 通道初始化器

                // 绑定端口并启动（非阻塞？不，bind().sync()是阻塞当前子线程，不影响主线程）
                ChannelFuture future = bootstrap.bind(nettyPort).sync();
                log.info("Netty服务端启动成功，端口：{}", nettyPort);

                // 等待服务端关闭（仅阻塞当前子线程，不影响Spring主线程/Tomcat）
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Netty服务端启动失败", e);
                Thread.currentThread().interrupt();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                log.info("Netty服务端已关闭:{}",nettyPort);
            }
        }, "netty-server-thread").start(); // 给线程命名，方便排查问题
    }

    /**
     * 关闭Netty服务（Spring销毁前执行）
     */
    @PreDestroy
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty服务端已关闭");
    }
}