package com.example.study.imserver.handler;


import com.example.study.imserver.util.ChannelManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Netty服务端通道初始化器（配置编解码器、业务处理器）
 * @author 编程导师
 */
@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private ImServerHandler imServerHandler;


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 字符串编解码器（统一使用UTF-8）
        pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
        pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));

        // 自定义业务处理器
        pipeline.addLast(imServerHandler);
    }
}