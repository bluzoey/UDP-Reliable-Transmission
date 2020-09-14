package Client;

import Tool.Tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Client {

    protected static final int DEST_PORT = 9999;
    private static final int SEND_PORT = 10000;
    private static final int RECE_PORT = 6666;

    protected static DatagramSocket clientSocket=null;

    protected static Map<Long,byte[]> map = new HashMap<>();

    Client(){
        try {
            clientSocket = new DatagramSocket(RECE_PORT);
            clientSocket.setSoTimeout(10000);
        }catch (SocketException e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Client client=new Client();

        ResendThread resend=new ResendThread();
        Thread resendThread=new Thread(resend);
        resendThread.start();

        FileInputStream fis = null;
        try {
            InetAddress ip=InetAddress.getByName("192.168.199.134");

            String path="E:\\d.mp4";
            File file=new File(path);
            fis=new FileInputStream(file);
            long fileLength=file.length();

            //计算分组数，计划每个数据包包含1024 byte有效数据
            long groupQuantity=(fileLength%1024==0)?(fileLength/1024):((fileLength/1024)+1);

            //发送
            //每次读入1024 byte
            long seq=0;
            byte[] readByte=new byte[1024];
            while((fis.read(readByte))>0){
                //包序号从1开始
                seq++;

                ByteBuffer headBuff=ByteBuffer.allocate(16);
                headBuff.putLong(seq);
                headBuff.putLong(groupQuantity);

                byte[] headByte=headBuff.array();

                int sendLen=headByte.length+readByte.length;//8+8+1024=1040；
                byte[] sendByte=new byte[sendLen];

                //将headByte和有效数据拼装成sendByte
                System.arraycopy(headByte,0,sendByte,0,headByte.length);
                System.arraycopy(readByte,0,sendByte,headByte.length,readByte.length);

                //sendByte存入map，重传时复用
                map.put(seq,sendByte);

                DatagramPacket sendPacket=new DatagramPacket(sendByte,sendByte.length,ip,DEST_PORT);
                clientSocket.send(sendPacket);

                System.out.println("sending: seq:"+seq+" quantity: "+groupQuantity);

                //控制传输速率
                Thread.sleep(1);
            }

            byte[] receiveByte = new byte[1024];
            while (true) {
                DatagramPacket getPacket = new DatagramPacket(receiveByte, 0, receiveByte.length);
                try {
                   clientSocket.receive(getPacket);
                } catch (SocketTimeoutException e){
                    System.out.println("time out, receive ends" + "resend ends");
                    resend.endThread(true);
                    break;
                }catch (IOException e) {
                    e.printStackTrace();
                }
                int k = 0;
                while (k != 1024 / 8) {
                    byte[] resendNumberByte = Arrays.copyOfRange(receiveByte, k * 8, k * 8 + 8);
                    long resendNumber = Tool.bytesToLong(resendNumberByte);
                    if (resendNumber != 0) {
                        //保证resendNumber已发出
                        if(!resend.getResend()) {
                            //System.out.println("need to resend: "+resendNumber);
                            k++;
                            resend.setResendNumber(resendNumber);
                            resend.setDestIp(ip);
                            resend.setResend(true);
                        }
                    }else if(resendNumber==0){
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(clientSocket!=null){
                    clientSocket.close();
                }if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

