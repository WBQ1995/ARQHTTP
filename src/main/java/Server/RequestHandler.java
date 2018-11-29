package Server;

import Packet.Packet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RequestHandler implements Runnable {

    Packet rcvPacket;
    NewConnection connection;
    Packet resPacket;

    public RequestHandler(Packet rcvPacket, NewConnection connection){
        this.rcvPacket = rcvPacket;
        this.connection = connection;
    }

    public void run(){

        int packetType = rcvPacket.getType();

        if(packetType == 3){
            String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
            if(Long.parseLong(payload) == connection.getSequenceNumber())
                connection.setConnected(true);
        } else {

            if(packetType == 0){
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);

                connection.setStartNumber(1);
                connection.setEndNumber(1);
                connection.getRcvWindow().put(connection.getSequenceNumber(), payload);

                resPacket = new Packet.Builder()
                        .setType(4)
                        .setSequenceNumber(rcvPacket.getSequenceNumber())
                        .setPortNumber(rcvPacket.getPeerPort())
                        .setPeerAddress(rcvPacket.getPeerAddress())
                        .setPayload("".getBytes())
                        .create();

                try {
                    connection.getChannel().send(resPacket.toBuffer(),connection.getRouterAddress());
                } catch (IOException ex){
                    ex.getStackTrace();
                }

            } else if(packetType == 5){
                if(connection.getStartNumber() == -1)
                    connection.setStartNumber(rcvPacket.getSequenceNumber());
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
                connection.getRcvWindow().put(rcvPacket.getSequenceNumber(),payload);
            } else if(packetType == 6){
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
                connection.getRcvWindow().put(rcvPacket.getSequenceNumber(),payload);
            } else if(packetType == 7){
                if(connection.getEndNumber() == -1)
                    connection.setEndNumber(rcvPacket.getSequenceNumber());
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
                connection.getRcvWindow().put(rcvPacket.getSequenceNumber(),payload);
            }

            resPacket = new Packet.Builder()
                    .setType(4)
                    .setSequenceNumber(rcvPacket.getSequenceNumber())
                    .setPortNumber(rcvPacket.getPeerPort())
                    .setPeerAddress(rcvPacket.getPeerAddress())
                    .setPayload("".getBytes())
                    .create();
            try {
                connection.getChannel().send(resPacket.toBuffer(),connection.getRouterAddress());
            } catch (IOException ex){
                ex.getStackTrace();
            }

        }
    }

}
