package com.example.study.nettynio.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/1 21:09
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.bind(new InetSocketAddress(6066));

        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            int select = selector.select(1000);
            if (select == 0) {
                System.out.println("没有事件发生");
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    try {
                        System.out.println("有新的连接,selectionKey:" + selectionKey.hashCode());
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        if (socketChannel != null) {
                            socketChannel.configureBlocking(false);
                            // 注册客户端，绑定客户到selector,同时继续关注读时间，分配对应的buffer
                            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (selectionKey.isReadable()) {
                    System.out.println("有数据可读");
                    SocketChannel channel = (SocketChannel)selectionKey.channel();
                    ByteBuffer byteBuffer = (ByteBuffer)selectionKey.attachment();
                    try {
                        channel.read(byteBuffer);
                        System.out.println("read from client :" + new String(byteBuffer.array()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (selectionKey.isWritable()) {
                    System.out.println("有数据可写");
                }
            }
        }

    }
}
