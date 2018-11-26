package Client;

import Packet.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class httpc {

    public static void main(String[] args) throws InterruptedException{
        try {
            Client client = new Client();
            client.handShake();
            Thread.sleep(100);

            client.sendData((client.getAckNumber() + 1) + "" + "Hello there!");

            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            while (true){
                buf.clear();
                client.getChannel().receive(buf);
                buf.flip();
                Packet resp = Packet.fromBuffer(buf);
                if(resp.getType() == 4){
                    client.setConnected(true);
                    client.getWindow().put(resp.getSequenceNumber(),true);
                    //String payload = new String(resp.getPayload(),UTF_8);
                    //System.out.println(payload);
                    if(client.allSent())
                        break;
                }
            }
            System.out.println("Client done!");

        } catch (IOException ex){
            ex.getStackTrace();
        }
    }
}
