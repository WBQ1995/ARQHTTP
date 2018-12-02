package Client;

import Packet.Packet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResponseHandler implements Runnable {

    private Packet rcvPacket;
    private Client client;
    Packet resPacket;

    public ResponseHandler(Packet packet, Client client){
        this.rcvPacket = packet;
        this.client = client;
    }

    public void run(){
        int packetType = rcvPacket.getType();

        if(packetType == 0){
            String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);

            client.setConnected(true);

            client.setStartNumber(1);
            client.setEndNumber(1);
            client.getRcvWindow().put(client.getSequenceNumber(), payload);

            resPacket = new Packet.Builder()
                    .setType(4)
                    .setSequenceNumber(rcvPacket.getSequenceNumber())
                    .setPortNumber(rcvPacket.getPeerPort())
                    .setPeerAddress(rcvPacket.getPeerAddress())
                    .setPayload("".getBytes())
                    .create();
            try {
                client.getChannel().send(resPacket.toBuffer(),client.getRouterAddress());
            } catch (IOException ex){
                ex.getStackTrace();
            }

        } else if(packetType == 5){

            client.setConnected(true);

            if(client.getStartNumber() == -1)
                client.setStartNumber(rcvPacket.getSequenceNumber());
            String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
            client.getRcvWindow().put(rcvPacket.getSequenceNumber(),payload);
        } else if(packetType == 6){
            String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
            client.getRcvWindow().put(rcvPacket.getSequenceNumber(),payload);
        } else if(packetType == 7){
            if(client.getEndNumber() == -1)
                client.setEndNumber(rcvPacket.getSequenceNumber());
            String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
            client.getRcvWindow().put(rcvPacket.getSequenceNumber(),payload);
        }
        resPacket = new Packet.Builder()
                .setType(4)
                .setSequenceNumber(rcvPacket.getSequenceNumber())
                .setPortNumber(rcvPacket.getPeerPort())
                .setPeerAddress(rcvPacket.getPeerAddress())
                .setPayload("".getBytes())
                .create();
        try {
            client.getChannel().send(resPacket.toBuffer(),client.getRouterAddress());
        } catch (IOException ex){
            ex.getStackTrace();
        }

    }
}
