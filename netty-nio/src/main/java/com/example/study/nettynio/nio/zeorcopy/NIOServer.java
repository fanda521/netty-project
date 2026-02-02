package com.example.study.nettynio.nio.zeorcopy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/2 17:41
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7001);

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(inetSocketAddress);

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);


        String saveFileName = "recv_001.mp4";
        FileOutputStream fos = new FileOutputStream(saveFileName);


        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            int readCount = 0;
            // ========== 新增：记录接收开始时间 ==========
            long startTime = System.currentTimeMillis();
            while (-1 != readCount) {
                try {
                    socketChannel.read(byteBuffer);
                    fos.write(byteBuffer.array());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byteBuffer.rewind();
            }

            // ========== 新增：记录接收结束时间并计算耗时 ==========
            long endTime = System.currentTimeMillis();
            long costTimeMs = endTime - startTime;
            double costTimeS = costTimeMs / 1000.0;

            System.out.printf("接收耗时：%d 毫秒 (%.2f 秒)%n", costTimeMs, costTimeS);
        }
    }
}
