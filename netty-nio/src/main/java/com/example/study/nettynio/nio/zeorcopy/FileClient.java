package com.example.study.nettynio.nio.zeorcopy;

import java.io.*;
import java.net.Socket;

/**
 * Socket文件发送客户端
 */
public class FileClient {
    // 服务端IP（本地测试用127.0.0.1，远程需改对应IP）
    private static final String SERVER_IP = "127.0.0.1";
    // 服务端端口（需和服务端一致）
    private static final int SERVER_PORT = 8888;
    // 每次发送的缓冲区大小（4KB）
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        // 要发送的文件路径（替换成你自己的文件路径，比如 "D:/test.txt" 或 "/Users/test.jpg"）
        String filePath = "R:\\repository\\ai-song\\soul-20250622120812.mp4";
        sendFile(filePath);
    }

    /**
     * 发送文件到服务端
     * @param filePath 本地文件路径
     */
    private static void sendFile(String filePath) {
        File file = new File(filePath);
        // 校验文件是否存在
        if (!file.exists()) {
            System.err.println("错误：文件不存在！路径：" + filePath);
            return;
        }
        // 校验是否是文件（不是文件夹）
        if (!file.isFile()) {
            System.err.println("错误：指定路径不是文件！路径：" + filePath);
            return;
        }

        Socket socket = null;
        try {
            // 1. 连接服务端
            socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("已连接到服务端：" + SERVER_IP + ":" + SERVER_PORT);

            // 2. 获取输出流（DataOutputStream方便发送基本类型）
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // 3. 准备文件元数据
            String fileName = file.getName();
            long fileSize = file.length();
            System.out.println("准备发送文件：" + fileName + "，文件大小：" + fileSize + " 字节");

            // 4. 发送文件元数据
            // ========== 新增：记录接收开始时间 ==========
            long startTime = System.currentTimeMillis();
            // 第一步：发送文件名长度（int类型，4字节）
            byte[] fileNameBytes = fileName.getBytes("UTF-8");
            dos.writeInt(fileNameBytes.length);
            // 第二步：发送文件名
            dos.write(fileNameBytes);
            // 第三步：发送文件大小（long类型，8字节）
            dos.writeLong(fileSize);

            // 5. 分块读取文件并发送
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            long sentSize = 0;
            int readLen;

            while ((readLen = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, readLen);
                sentSize += readLen;
                // 打印发送进度
                //double progress = (double) sentSize / fileSize * 100;
                //System.out.printf("发送进度：%d/%d 字节 (%.1f%%)%n", sentSize, fileSize, progress);
            }


            // ========== 新增：记录接收结束时间并计算耗时 ==========
            long endTime = System.currentTimeMillis();
            long costTimeMs = endTime - startTime;
            double costTimeS = costTimeMs / 1000.0;

            System.out.printf("接收耗时：%d 毫秒 (%.2f 秒)%n", costTimeMs, costTimeS);
            // 刷新输出流，确保所有数据发送完成
            dos.flush();

            System.out.println("文件发送完成！");

            // 关闭文件输入流
            fis.close();
            // 关闭数据输出流
            dos.close();

        } catch (IOException e) {
            System.err.println("客户端异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 释放Socket资源
            try {
                if (socket != null) socket.close();
                System.out.println("客户端连接已关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}