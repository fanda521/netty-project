package com.example.study.nettynio.nio.channel;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/1 19:46
 */
public class ChannelTest {

    private final String path = System.getProperty("user.dir") + "/src/main/java/com/example/study/nettynio/nio/channel/file/writDest/";
    @Test
    public void testWriteToFile() throws IOException {
        // 写入的目标地址
        // 获取项目的当前路径
        String filePath = path + "test.txt";
        System.out.println( "filePath:" + filePath);
        try (FileOutputStream fos = new FileOutputStream(filePath)){
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put("hello world,小明".getBytes());
            byteBuffer.flip();

            fos.getChannel().write(byteBuffer);
            fos.close();
        }
    }

    @Test
    public void testReadFromFile() throws IOException {
        String filePath = path + "test.txt";
        try (FileInputStream fis = new FileInputStream(filePath)){
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            FileChannel channel = fis.getChannel();
            channel.read(byteBuffer);
            System.out.println(new String(byteBuffer.array()));
        }
    }

    @Test
    public void testCopyFile() throws IOException {
        String sourcePath = path + "test.txt";
        String destPath = path + "testCopy.txt";
        try (FileInputStream fis = new FileInputStream(sourcePath);
             FileOutputStream fos = new FileOutputStream(destPath)){
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            FileChannel sourceChannel = fis.getChannel();
            FileChannel destChannel = fos.getChannel();
            while (true){
                byteBuffer.clear();
                int read = sourceChannel.read(byteBuffer);
                if (read == -1){
                    break;
                }
                byteBuffer.flip();
                destChannel.write(byteBuffer);
            }
        }
    }

    @Test
    public void testTransferFrom() throws IOException {
        String sourcePath = path + "test.txt";
        String destPath = path + "testTransferFrom.txt";
        try (FileInputStream fis = new FileInputStream(sourcePath);
             FileOutputStream fos = new FileOutputStream(destPath)){
            FileChannel sourceChannel = fis.getChannel();
            FileChannel destChannel = fos.getChannel();
            destChannel.transferFrom(sourceChannel,0,sourceChannel.size());
        }
    }





}
