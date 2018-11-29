package Client;

import Packet.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

public class httpc {

    public static void main(String[] args) throws InterruptedException{
        try {
            Client client = new Client();
            client.handShake();
            Thread.sleep(100);

            client.sendData("get http://localhost:8008/");

            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            while (true){
                buf.clear();
                client.getChannel().receive(buf);
                buf.flip();
                Packet resp = Packet.fromBuffer(buf);
                if(resp.getType() == 4){
                    client.setConnected(true);
                    client.getSendWindow().put(resp.getSequenceNumber(),true);
                    if(client.allSent())
                        break;
                }
            }
            System.out.println("Request has been sent!");
            client.increaseAckNumber();

            ResponseProcesser responseProcesser = new ResponseProcesser(client);
            Thread processResponseThread = new Thread(responseProcesser);
            processResponseThread.start();

            Thread.sleep(2000);

            String data = "";
            for (long key:client.getRcvWindow().keySet()) {
                data += client.getRcvWindow().get(key);
            }
            System.out.println(data);

            System.out.println("client done!");

        } catch (IOException ex){
            ex.getStackTrace();
        }
    }
}
