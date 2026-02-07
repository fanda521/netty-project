package com.example.study.nettyself.dubbo.provider;

import com.example.study.nettyself.dubbo.netty.NettyServer;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/7 22:29
 */
public class ServerBootstrap {
    public static void main(String[] args) {
        NettyServer.startServer("127.0.0.1", 8088);;
    }
}
