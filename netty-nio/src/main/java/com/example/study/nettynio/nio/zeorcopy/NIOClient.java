package com.example.study.nettynio.nio.zeorcopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/2 17:50
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 7001));

        String filePath = "R:\\repository\\ai-song\\soul-20250622120812.mp4";

        FileChannel channel = new FileInputStream(filePath).getChannel();
        // ========== 新增：记录接收开始时间 ==========
        long startTime = System.currentTimeMillis();

        channel.transferTo(0, channel.size(), socketChannel);

        // ========== 新增：记录接收结束时间并计算耗时 ==========
        long endTime = System.currentTimeMillis();
        long costTimeMs = endTime - startTime;
        double costTimeS = costTimeMs / 1000.0;

        System.out.printf("接收耗时：%d 毫秒 (%.2f 秒)%n", costTimeMs, costTimeS);
    }
}
