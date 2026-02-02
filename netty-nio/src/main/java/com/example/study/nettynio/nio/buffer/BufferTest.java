package com.example.study.nettynio.nio.buffer;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author lucksoul
 * @version 1.0
 * @date 2026/2/1 19:30
 */
public class BufferTest {

    private final String path = System.getProperty("user.dir") + "/src/main/java/com/example/study/nettynio/nio/channel/file/writDest/";

    /**
     * buffer 有四个属性
     * mark
     * position :当前读的位置
     * limit:缓冲区中可以操作的元素的最大数
     * capacity:缓冲区大小
     * position < limit < capacity
     *
     */

    @Test
    public void testRead() {
        IntBuffer allocate = IntBuffer.allocate(20);
        allocate.put(1);
        allocate.put(2);
        allocate.put(3);

        allocate.flip();

        while (allocate.hasRemaining()) {
            System.out.println(allocate.get());
        }
    }

    @Test
    public void testReadType() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        byteBuffer.putInt(12);
        byteBuffer.putChar('a');
        byteBuffer.putShort((short) 12);
        byteBuffer.putLong(12);
        byteBuffer.putFloat(12.12f);
        byteBuffer.putDouble(12.12);

        byteBuffer.flip();
        int anInt = byteBuffer.getInt();

        char c = byteBuffer.getChar();
        short s = byteBuffer.getShort();
        long l = byteBuffer.getLong();
        float f = byteBuffer.getFloat();
        double d = byteBuffer.getDouble();

        System.out.println(anInt);
        System.out.println(c);
        System.out.println(s);
        System.out.println(l);
        System.out.println(f);
        System.out.println(d);



    }

    @Test
    public void  testReadOnly() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.putInt(12);
        byteBuffer.flip();
        ByteBuffer byteBufferReadOnly = byteBuffer.asReadOnlyBuffer();

        //byteBufferReadOnly.putInt(14);//java.nio.ReadOnlyBufferException

    }

    @Test
    public void testMappedByBuffer() throws IOException {
        RandomAccessFile rw = new RandomAccessFile(path + "test.txt", "rw");
        FileChannel channel = rw.getChannel();
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
        map.put(0, (byte) 'a');
        map.put(3, (byte) 'b');
    }
}
