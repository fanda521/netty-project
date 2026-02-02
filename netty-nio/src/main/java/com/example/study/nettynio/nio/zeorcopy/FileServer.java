package com.example.study.nettynio.nio.zeorcopy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket文件接收服务端
 */
public class FileServer {
    // 监听端口
    private static final int PORT = 8888;
    // 每次读取的缓冲区大小（4KB）
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        // 声明ServerSocket和客户端Socket，放在try外方便finally关闭
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            // 1. 创建ServerSocket并绑定端口
            serverSocket = new ServerSocket(PORT);
            System.out.println("服务端已启动，监听端口：" + PORT + "，等待客户端连接...");

            // 2. 阻塞等待客户端连接
            clientSocket = serverSocket.accept();
            System.out.println("客户端已连接：" + clientSocket.getInetAddress().getHostAddress());

            // 3. 获取输入流（DataInputStream方便读取基本类型）
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

            // 4. 读取文件元数据
            // 第一步：读取文件名长度（int类型，4字节）
            int fileNameLength = dis.readInt();
            // 第二步：读取文件名（按长度读取，避免乱码）
            byte[] fileNameBytes = new byte[fileNameLength];
            dis.readFully(fileNameBytes); // 确保读取完整的字节数
            String fileName = new String(fileNameBytes, "UTF-8");
            // 第三步：读取文件大小（long类型，8字节）
            long fileSize = dis.readLong();

            System.out.println("开始接收文件：" + fileName + "，文件大小：" + fileSize + " 字节");

            // 5. 接收文件内容并写入本地（添加recv_前缀避免覆盖）
            // ========== 新增：记录接收开始时间 ==========
            long startTime = System.currentTimeMillis();
            String saveFileName = "recv_" + fileName;
            FileOutputStream fos = new FileOutputStream(saveFileName);
            byte[] buffer = new byte[BUFFER_SIZE];
            long receivedSize = 0;
            int readLen;

            // 分块读取文件内容
            while (receivedSize < fileSize && (readLen = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, readLen);
                receivedSize += readLen;
                // 打印接收进度
                //double progress = (double) receivedSize / fileSize * 100;
                //System.out.printf("接收进度：%d/%d 字节 (%.1f%%)%n", receivedSize, fileSize, progress);
            }

            // ========== 新增：记录接收结束时间并计算耗时 ==========
            long endTime = System.currentTimeMillis();
            long costTimeMs = endTime - startTime;
            double costTimeS = costTimeMs / 1000.0;

            // 验证是否接收完整
            if (receivedSize == fileSize) {
                System.out.println("文件接收完成！保存路径：" + new File(saveFileName).getAbsolutePath());
                System.out.printf("接收耗时：%d 毫秒 (%.2f 秒)%n", costTimeMs, costTimeS);
            } else {
                System.err.println("文件接收不完整！已接收：" + receivedSize + " 字节，预期：" + fileSize + " 字节");
            }

            // 关闭文件输出流
            fos.close();
            // 关闭数据输入流
            dis.close();

        } catch (IOException e) {
            System.err.println("服务端异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 释放资源
            try {
                if (clientSocket != null) clientSocket.close();
                if (serverSocket != null) serverSocket.close();
                System.out.println("服务端资源已释放");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}