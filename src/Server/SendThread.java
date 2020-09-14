package Server;

import Tool.Tool;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class SendThread implements Runnable {

    private boolean send = false;
    private boolean end = false;
    private boolean isNumberListInit = false;

    private InetAddress destIp;

    protected static ArrayList<Long> numberList = new ArrayList<>();

    public void setdestIp(InetAddress destIp1) {
        destIp = destIp1;
    }

    public void setSend(boolean send1) {
        send = send1;
    }

    public void endThread(boolean end1) {
        end = end1;
    }

    public void initNumberList(long groupQuantity) {
        if (!isNumberListInit) {
            for (long i = 1; i <= groupQuantity; i++) {
                numberList.add(i);
            }
            isNumberListInit = true;
        }
    }

    public void getResendNumberList() {
        for (int i = 0; i < numberList.size(); i++) {
            if (Server.map.containsKey(numberList.get(i))) {
                numberList.remove(i);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (send) {
                getResendNumberList();
                //发送缺少的包序号，请求重传
                int k = 0;
                byte sendByte[] = new byte[1024];
                for (int i = 0; i < numberList.size(); i++) {
                    byte[] number = Tool.longToBytes(numberList.get(i));
                    System.arraycopy(number, 0, sendByte, k * 8, 8);
                    k++;
                    //System.out.println("缺 seq：" + numberList.get(i));
                    if (k == 1024 / 8 - 1) {
                        k = 0;
                        DatagramPacket sendPacket = new DatagramPacket(sendByte, sendByte.length, destIp, Server.DEST_PORT);
                        try {
                            Server.serverSocket.send(sendPacket);
                            Thread.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
                setSend(false);
            }
            if (end) {
                break;
            }
        }
    }
}
