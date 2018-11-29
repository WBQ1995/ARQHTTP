package Server;

import Packet.Packet;

import java.io.IOException;

public class SendPacket implements Runnable {

    Packet packet;
    NewConnection connection;

    SendPacket(Packet packet, NewConnection connection){
        this.packet = packet;
        this.connection = connection;
    }

    public void run(){

        System.out.println("packet# " + packet.getSequenceNumber());
        System.out.println(packet.getType());

        while (!connection.getSendWindow().get(packet.getSequenceNumber())){
            try {
                connection.getChannel().send(packet.toBuffer(), connection.getRouterAddress());

                Thread.sleep(100);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
