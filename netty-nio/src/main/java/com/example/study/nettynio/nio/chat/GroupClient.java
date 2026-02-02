package com.example.study.nettynio.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/2 1:44
 */
public class GroupClient {

    private static final int PORT = 6066;
    private static final String HOST = "127.0.0.1";

    private SocketChannel socketChannel;
    private Selector selector;
    private String username ;

    public GroupClient() throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        username = socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(username + " is ok");
    }

    public void sendMsg(String msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        socketChannel.write(buffer);
    }


    public void start() {

        while (true) {
            try {
                int select = selector.select();
                if (select == 0) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        SelectableChannel channel = selectionKey.channel();
                        try {
                            if (channel != null) {
                                SocketChannel socketChannel = (SocketChannel) channel;
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                socketChannel.read(buffer);
                                System.out.println("read from server :" + socketChannel.getRemoteAddress() + ", say:" + new String(buffer.array()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // 记住删除当前的时间，很重要
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        GroupClient groupClient = new GroupClient();
        new Thread(
            () -> {
                groupClient.start();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        ).start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String msg = scanner.next();
            System.out.println("send to server:" + msg);
            groupClient.sendMsg(msg);
        }
    }
}
