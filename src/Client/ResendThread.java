package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ResendThread implements Runnable {

    private boolean resend = false;
    private boolean end = false;
    private InetAddress destIp;

    private long resendNumber;

    public boolean getResend(){
        return resend;
    }

    public void setResend(boolean resend1) {
        resend = resend1;
    }

    public void setDestIp(InetAddress destIp1) {
        destIp = destIp1;
    }

    public void setResendNumber(long resendNumber1) {
        resendNumber = resendNumber1;
    }

    public void endThread(boolean end1) {
        end = end1;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (resend) {
                System.out.println("resending: " + resendNumber);
                DatagramPacket resendPacket = new DatagramPacket(Client.map.get(resendNumber), Client.map.get(resendNumber).length, destIp, Client.DEST_PORT);
                try {
                    Client.clientSocket.send(resendPacket);
                    //Thread.sleep(1);
                } catch (IOException e) {
                    e.printStackTrace();// TODO: 2020/9/8
                }
                setResend(false);
            }
            if(end){
                break;
            }
        }
    }
}
