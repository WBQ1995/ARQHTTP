package Server;

import Packet.Packet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RequestHandler implements Runnable {

    Packet rcvPacket;
    NewConnection connection;
    Packet resPackage;

    public RequestHandler(Packet rcvPacket, NewConnection connection){
        this.rcvPacket = rcvPacket;
        this.connection = connection;
    }

    public void run(){

        int packageType = rcvPacket.getType();

        if(packageType == 3){
            String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
            if(Long.parseLong(payload) == connection.getSequenceNumber())
                connection.setConnected(true);
        } else {

            // TODO: 2018-11-26 this is a incorrect way to close processRequest, have to fix later 
            if(packageType == 100){
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
                String data = payload.substring((connection.getSequenceNumber() + "").length());
                if(payload.startsWith(connection.getSequenceNumber() + "")) {
                    if (!connection.getFin()) {
                        connection.setFin(true);

                        connection.getWindow().put(connection.getSequenceNumber(),data);
                        //System.out.println(connection.getRcvData());
                    }
                }
                resPackage = new Packet.Builder()
                        .setType(4)
                        .setSequenceNumber(rcvPacket.getSequenceNumber())
                        .setPortNumber(rcvPacket.getPeerPort())
                        .setPeerAddress(rcvPacket.getPeerAddress())
                        .setPayload("".getBytes())
                        .create();
                try {
                    connection.getChannel().send(resPackage.toBuffer(),connection.getRouterAddress());
                } catch (IOException ex){
                    ex.getStackTrace();
                }

            } else if(packageType == 4){
                if(connection.getStartNumber() == -1)
                    connection.setStartNumber(rcvPacket.getSequenceNumber());
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
                payload = payload.substring((connection.getSequenceNumber() + "").length());
                connection.getWindow().put(rcvPacket.getSequenceNumber(),payload);
            } else if(packageType == 5){
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
                connection.getWindow().put(rcvPacket.getSequenceNumber(),payload);
            } else if(packageType == 6){
                if(connection.getEndNumber() == -1)
                    connection.setEndNumber(rcvPacket.getSequenceNumber());
                String payload = new String(rcvPacket.getPayload(), StandardCharsets.UTF_8);
                connection.getWindow().put(rcvPacket.getSequenceNumber(),payload);


            }

//            String data = "";
//            for (long key:connection.getWindow().keySet()){
//                data += connection.getWindow().get(key);
//            }
//
//            System.out.println(data);

            if(connection.getStartNumber() != -1L && connection.getEndNumber() != -1L &&
                    connection.getWindow().size() == (int)(connection.getEndNumber() - connection.getStartNumber() + 1)){
                connection.allRecieved = true;
            }

            resPackage = new Packet.Builder()
                    .setType(4)
                    .setSequenceNumber(rcvPacket.getSequenceNumber())
                    .setPortNumber(rcvPacket.getPeerPort())
                    .setPeerAddress(rcvPacket.getPeerAddress())
                    .setPayload("".getBytes())
                    .create();
            try {
                connection.getChannel().send(resPackage.toBuffer(),connection.getRouterAddress());
            } catch (IOException ex){
                ex.getStackTrace();
            }

        }
    }

}
