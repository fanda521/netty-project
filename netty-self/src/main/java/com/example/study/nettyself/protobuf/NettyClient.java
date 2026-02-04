package com.example.study.nettyself.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.Scanner;

/**
 * Netty 4.x 客户端核心启动类
 * 功能：连接服务端，发送消息，接收服务端回复
 */
public class NettyClient {
    // 服务端IP和端口
    private final String host;
    private final int port;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 启动客户端并连接服务端
     */
    public void start() throws InterruptedException {
        // ========== 客户端事件循环组 ==========
        // 客户端只需一个EventLoopGroup（无需区分Boss/Worker），处理连接和读写
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            // ========== 客户端启动器：Bootstrap ==========
            // 区别于服务端的ServerBootstrap，客户端使用Bootstrap
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    // 绑定事件循环组
                    .group(group)
                    // 设置客户端通道类型为NioSocketChannel
                    .channel(NioSocketChannel.class)
                    // 配置通道参数：开启TCP心跳机制
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // 初始化通道处理器链
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("protobufEn",new ProtobufEncoder())
                                    .addLast("protobufDe", new ProtobufDecoder(StudentPOJO.Student.getDefaultInstance()))
                                    // 自定义客户端处理器
                                    .addLast(new ClientHandler());
                        }
                    });

            // ========== 连接服务端 ==========
            ChannelFuture future = bootstrap.connect(host, port).sync();
            System.out.println("客户端已成功连接服务端：" + host + ":" + port);

            // ========== 读取控制台输入并发送消息 ==========
            Channel channel = future.channel();
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入要发送的消息（输入exit退出）：");
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                if ("exit".equalsIgnoreCase(msg)) {
                    // 关闭通道并退出
                    channel.close();
                    break;
                }
                // 发送消息到服务端
                StudentPOJO.Student.Builder student = StudentPOJO.Student.newBuilder().setId(2).setName(msg);
                channel.writeAndFlush(student);
            }

            // 等待通道关闭
            channel.closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组
            group.shutdownGracefully();
            System.out.println("客户端已优雅关闭");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 连接本地8888端口的服务端
        new NettyClient("127.0.0.1", 8888).start();
    }
}