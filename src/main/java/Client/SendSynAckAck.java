package Client;

import Packet.Packet;

import java.io.IOException;

public class SendSynAckAck implements Runnable {

    private long mySequenceNumber;
    private Client client;

    public SendSynAckAck(long sequenceNumber, Client client){
        this.client = client;
        this.mySequenceNumber = sequenceNumber;
    }

    public void run(){
        client.increaseSequenceNumber();

        Packet synAckAck = new Packet.Builder()
                .setType(3)
                .setSequenceNumber(mySequenceNumber)
                .setPortNumber(client.getServerAddress().getPort())
                .setPeerAddress(client.getServerAddress().getAddress())
                .setPayload("".getBytes())
                .create();

        while (client.getSequenceNumber() == mySequenceNumber + 1){
            try {
                client.getChannel().send(synAckAck.toBuffer(), client.getRouterAddress());

                Thread.sleep(50);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
