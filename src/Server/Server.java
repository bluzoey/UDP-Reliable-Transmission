package Server;

import Tool.Tool;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server {

    protected static final int DEST_PORT = 6666;
    private static final int RECE_PORT = 9999;

    //存放包中的有效数据，结构：包序号->该序号的有效数据
    protected static Map<Long, byte[]> map = new HashMap<>();

    protected static DatagramSocket serverSocket=null;

    Server(){
        try {
            serverSocket = new DatagramSocket(RECE_PORT);
            serverSocket.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server=new Server();
        try {
            Thread.sleep(2000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        SendThread send = new SendThread();
        Thread sendThread = new Thread(send);
        sendThread.start();

        //readByte：0-7包序号，8-15总包数，15-1023有效数据
        byte[] readByte = new byte[8 + 8 + 1024];
        long groupQuantity = 0;
        long timeOut = -1;

        while (true) {
            DatagramPacket getPacket = new DatagramPacket(readByte, 0, readByte.length);
            try {
                serverSocket.receive(getPacket);
            } catch (SocketTimeoutException e) {
                if (System.currentTimeMillis() - timeOut >= 1000) {
                    //超时情况下，又从未收到包，关闭发送线程,关闭接收
                    if (groupQuantity == 0) {
                        send.endThread(true);
                        break;
                    } else if (groupQuantity != 0) {
                        try {
                            send.setdestIp(InetAddress.getLocalHost());
                        }catch (UnknownHostException ue){
                            ue.printStackTrace();
                        }
                        send.initNumberList(groupQuantity);
                        send.setSend(true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte seqByte[] = Arrays.copyOfRange(readByte, 0, 8);
            byte groupQuantityByte[] = Arrays.copyOfRange(readByte, 8, 16);
            byte data[] = Arrays.copyOfRange(readByte, 16, 16 + 1024);

            readByte=new byte[8+8+1024];

            long seq = Tool.bytesToLong(seqByte);
            groupQuantity = Tool.bytesToLong(groupQuantityByte);

            if(seq!=0 && groupQuantity!=0) {
                timeOut = System.currentTimeMillis();
                System.out.println("receive: seq: " + seq + " quantity: " + groupQuantity);
                if (!Server.map.containsKey(seq)) {
                    Server.map.put(seq, data);
                    System.out.println("map put seq: " + seq);
                }
            }

            if (Server.map.size() == groupQuantity) {
                break;
            }
        }

        FileOutputStream fos = null;
        String path = "E:\\z.mp4";
        try {
            fos = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //将map中的有效数据写入文件
        for (long seq = 1; seq <=groupQuantity; seq++) {
            byte[] data = map.get(seq);
            try {
                System.out.println("write data； "+ seq);
                fos.write(data);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        try {
            if(serverSocket!=null){
                serverSocket.close();
            }
            if(fos!=null) {
                fos.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
