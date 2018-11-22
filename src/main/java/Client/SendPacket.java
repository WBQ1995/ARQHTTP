package Client;

import Packet.Packet;

import java.io.IOException;

public class SendPacket implements Runnable{

    Packet packet;
    Client client;
    long mySequenceNumber;

    SendPacket(Packet packet, Client client, long sequenceNumber){
            this.packet = packet;
            this.client = client;
            this.mySequenceNumber = sequenceNumber;
    }

    public void run(){
        client.increaseSequenceNumber();

        while (client.getSequenceNumber() == mySequenceNumber + 1){
            try {
                client.getChannel().send(packet.toBuffer(), client.getRouterAddress());

                Thread.sleep(50);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }

}
