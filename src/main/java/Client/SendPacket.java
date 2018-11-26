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
        //client.increaseSequenceNumber();

        System.out.println("packet# " + packet.getSequenceNumber());
        System.out.println(packet.getType());

        while (!client.getWindow().get(packet.getSequenceNumber())){
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
