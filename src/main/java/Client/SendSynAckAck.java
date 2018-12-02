package Client;

import Packet.Packet;

import java.io.IOException;

public class SendSynAckAck implements Runnable {

    private Client client;

    public SendSynAckAck(Client client){
        this.client = client;
    }

    public void run(){

        Packet synAckAck = new Packet.Builder()
                .setType(3)
                .setSequenceNumber(-1)
                .setPortNumber(client.getServerAddress().getPort())
                .setPeerAddress(client.getServerAddress().getAddress())
                .setPayload(((client.getAckNumber() + 1) + "").getBytes())
                .create();

        System.out.println("synAckAck# " + synAckAck.getSequenceNumber());

        while (!client.getConnected()){
            try {
                client.getChannel().send(synAckAck.toBuffer(), client.getRouterAddress());

                Thread.sleep(100);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
