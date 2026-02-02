package com.example.study.nettynio.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/1 21:36
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 6066));

        while (!socketChannel.finishConnect()) {
            System.out.println("正在连接中...");
        }
        System.out.println("连接成功");
        ByteBuffer byteBuffer = ByteBuffer.wrap("hello world".getBytes());
        socketChannel.write(byteBuffer);
        System.in.read();
    }
}
