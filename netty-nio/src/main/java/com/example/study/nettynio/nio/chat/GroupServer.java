package com.example.study.nettynio.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/2 1:44
 */
public class GroupServer {

    private static final int PORT = 6066;
    private ServerSocketChannel ssc ;
    private Selector selector;

    public GroupServer() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(PORT));

        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }


    public void start() throws IOException {
        while (true) {
            int select = selector.select();
            if (select == 0) {

            } else {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = ssc.accept();
                        socketChannel.configureBlocking(false);
                        System.out.println("有新的连接,selectionKey:" + socketChannel.getRemoteAddress());
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    else if (selectionKey.isReadable()) {
                        readMsg(selectionKey);
                    } else {

                    }
                    // 记住删除当前的时间，很重要
                    iterator.remove();
                }
            }
        }
    }

    public void readMsg(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer);
            String msg = new String(buffer.array());
            System.out.println("read from client :" + channel.getRemoteAddress() + ", say:" + msg);

            // 转发数据到其他客户端
            sendMsgToOthers(msg,channel);
        } catch (IOException e) {
            System.out.println(channel.getRemoteAddress() +" 下线了...");
            channel.close();
            selectionKey.cancel();
        }
    }


    public void sendMsgToOthers(String msg,SocketChannel self) throws IOException {
        Iterator<SelectionKey> iterator = selector.keys().iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            Channel channel = null;
            try {
                channel = selectionKey.channel();
                if (channel instanceof SocketChannel && channel != self) {
                    SocketChannel socketChannel = (SocketChannel)channel;
                    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                    socketChannel.write(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("群聊消息:" + msg + ". 转发完毕!");
    }

    public static void main(String[] args) {
        try {
            new GroupServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
