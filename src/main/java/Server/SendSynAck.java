package Server;


import Packet.Packet;

import java.io.IOException;

public class SendSynAck implements Runnable {

    private long mySequenceNumber;
    private NewConnection connection;

    public SendSynAck(long sequenceNumber, NewConnection connection){
        this.connection = connection;
        this.mySequenceNumber = sequenceNumber;
    }

    public void run(){
        connection.increaseSequenceNumber();

        Packet synAck = new Packet.Builder()
                .setType(2)
                .setSequenceNumber(mySequenceNumber)
                .setPortNumber(connection.getPacket().getPeerPort())
                .setPeerAddress(connection.getPacket().getPeerAddress())
                .setPayload("".getBytes())
                .create();

        while (connection.getSequenceNumber() == mySequenceNumber + 1){
            try {
                connection.getChannel().send(synAck.toBuffer(), connection.getRouterAddress());
                Thread.sleep(50);
            } catch (IOException ex){
                ex.getStackTrace();
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }
}
