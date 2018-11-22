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

            Packet data = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(client.getSequenceNumber())
                    .setPortNumber(client.getServerAddress().getPort())
                    .setPeerAddress(client.getServerAddress().getAddress())
                    .setPayload("Hello there".getBytes())
                    .create();
            client.sendData(data);

            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            while (true){
                buf.clear();
                client.getChannel().receive(buf);
                buf.flip();
                Packet resp = Packet.fromBuffer(buf);
                if(resp.getSequenceNumber() == client.getSequenceNumber()){
                    String payload = new String(resp.getPayload(),UTF_8);
                    System.out.println(payload);
                    client.increaseSequenceNumber();
                    break;
                }
            }
        } catch (IOException ex){
            ex.getStackTrace();
        }
    }
}
