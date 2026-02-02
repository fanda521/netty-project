package com.example.study.nettyself.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Netty 4.x 服务端核心启动类
 * 功能：监听指定端口，接收客户端连接，处理客户端消息并回复
 */
public class NettyServer {
    // 服务端监听端口
    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务端的核心方法
     */
    public void start() throws InterruptedException {
        // ========== 核心组件1：EventLoopGroup（事件循环组） ==========
        // 1. BossGroup：负责接收客户端的连接请求（仅处理连接建立，不处理读写）
        // NioEventLoopGroup：基于NIO的事件循环组，默认线程数为 CPU核心数 * 2
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 连接请求量小，设置1个线程即可
        // 2. WorkerGroup：负责处理已建立连接的读写操作
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 默认线程数，适合处理大量读写

        try {
            // ========== 核心组件2：ServerBootstrap（服务端启动器） ==========
            // 用于配置服务端的启动参数、绑定事件循环组、设置通道类型等
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    // 绑定两个事件循环组
                    .group(bossGroup, workerGroup)
                    // ========== 核心组件3：Channel（通道） ==========
                    // 设置服务端通道类型为NioServerSocketChannel（基于NIO的服务端Socket通道）
                    .channel(NioServerSocketChannel.class)
                    // ========== 通道参数配置 ==========
                    // SO_BACKLOG：设置TCP连接的等待队列大小（半连接队列），默认50
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // SO_KEEPALIVE：开启TCP心跳机制，保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // ========== 核心组件4：ChannelInitializer（通道初始化器） ==========
                    // 当新的客户端连接建立时，初始化通道的处理器链（Handler）
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 获取通道的处理器链（Pipeline），Netty的核心设计：责任链模式
                            ch.pipeline()
                                    // 字符串解码器：将ByteBuf（Netty字节缓冲区）解码为String
                                    .addLast(new StringDecoder())
                                    // 字符串编码器：将String编码为ByteBuf
                                    .addLast(new StringEncoder())
                                    // 自定义业务处理器：处理客户端消息
                                    .addLast(new ServerHandler());
                        }
                    });

            System.out.println("Netty服务端已启动，监听端口：" + port);

            // ========== 绑定端口并启动服务 ==========
            // bind()：绑定端口（非阻塞），sync()：阻塞等待绑定完成
            ChannelFuture future = serverBootstrap.bind(port).sync();

            // ========== 等待服务端通道关闭 ==========
            // closeFuture()：获取通道的关闭未来对象，sync()：阻塞等待服务端通道关闭（如手动停止）
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组，释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("Netty服务端已优雅关闭");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 启动服务端，监听8888端口
        new NettyServer(8888).start();
    }
}