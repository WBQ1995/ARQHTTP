package Client;

import Packet.Packet;

import java.io.IOException;

public class SendPacket implements Runnable{

    Packet packet;
    Client client;

    SendPacket(Packet packet, Client client){
            this.packet = packet;
            this.client = client;
    }

    public void run(){

        System.out.println("packet# " + packet.getSequenceNumber() + " packet type:" + packet.getType() + "\n");

        while (!client.getSendWindow().get(packet.getSequenceNumber())){
            try {
                client.getChannel().send(packet.toBuffer(), client.getRouterAddress());

                Thread.sleep(100);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }

}
