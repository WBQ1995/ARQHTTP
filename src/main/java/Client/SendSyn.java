package Client;

import Packet.Packet;

import java.io.IOException;

public class SendSyn implements Runnable {

    private Client client;

    public SendSyn(Client client){
       this.client = client;
    }

    public void run(){
        client.increaseSequenceNumber();

        Packet syn = new Packet.Builder()
                .setType(1)
                .setSequenceNumber(client.getSequenceNumber() - 1)
                .setPortNumber(client.getServerAddress().getPort())
                .setPeerAddress(client.getServerAddress().getAddress())
                .setPayload("".getBytes())
                .create();

        System.out.println("syn# " + syn.getSequenceNumber());

        while (client.getAckNumber() == -1){
            try {
                client.getChannel().send(syn.toBuffer(), client.getRouterAddress());
                Thread.sleep(100);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
