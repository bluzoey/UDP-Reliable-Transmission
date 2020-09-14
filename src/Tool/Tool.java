package Tool;

import java.nio.ByteBuffer;

public class Tool {
    //byte 数组与 long 的相互转换
    public static byte[] longToBytes(long x) {
        ByteBuffer bufferLong = ByteBuffer.allocate(8);
        bufferLong.putLong(0, x);
        return bufferLong.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer bufferLong = ByteBuffer.allocate(8);
        bufferLong.put(bytes, 0, bytes.length);
        bufferLong.flip();
        return bufferLong.getLong();
    }

    //byte 数组与 int 的相互转换
    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}
