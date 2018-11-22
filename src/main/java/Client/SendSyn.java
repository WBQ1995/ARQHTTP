package Client;

import Packet.Packet;

import java.io.IOException;

public class SendSyn implements Runnable {

    private long mySequenceNumber;
    private Client client;

    public SendSyn(long sequenceNumber, Client client){
       this.mySequenceNumber = sequenceNumber;
       this.client = client;
    }

    public void run(){
        client.increaseSequenceNumber();

        Packet syn = new Packet.Builder()
                .setType(1)
                .setSequenceNumber(mySequenceNumber)
                .setPortNumber(client.getServerAddress().getPort())
                .setPeerAddress(client.getServerAddress().getAddress())
                .setPayload("".getBytes())
                .create();

        while (client.getSequenceNumber() == mySequenceNumber + 1){
            try {
                client.getChannel().send(syn.toBuffer(), client.getRouterAddress());
                Thread.sleep(50);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
