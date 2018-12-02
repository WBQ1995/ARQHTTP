package Server;

import Packet.Packet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class NewConnection implements Runnable {

    private  SocketAddress router = new InetSocketAddress("localhost",3000);
    private  DatagramChannel channel;

    private long sequenceNumber;
    private long ackNumber;
    private Packet packet;
    private String path = "/";

    private boolean connected = false;

    private boolean allSent = false;

    private long startNumber = -1;
    private long endNumber = -1;
    private TreeMap<Long,String> rcvWindow;

    private HashMap<Long,Boolean> sendWindow;

    SlidingWindow slidingWindow;

    private ArrayList<Packet> packets;

    public NewConnection(Packet packet){

        packets = new ArrayList<>();

        this.packet = packet;
        int port = 9000 + (int)(Math.random() * 1000);

        ackNumber = packet.getSequenceNumber();
        sequenceNumber = (long) (Math.random() * 1000);

        rcvWindow = new TreeMap<>();
        sendWindow = new HashMap<>();

        try{
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(port));
        } catch (IOException ex){
            ex.getStackTrace();
        }
    }

    public void run(){
        try {
            sendSynAck();

            processRequest();

            String data = "";

            Thread.sleep(4000);

            if(!connected)
                return;

            for (long key: rcvWindow.keySet()){
                data += rcvWindow.get(key);
            }
            System.out.println("request received!\n");
            System.out.println(data);

            String response;
            Request request = new Request(data);
            if(request.isValidRequest()) {
                //response according to the message received
                Processor processor = new Processor(request, path);

                response = processor.getResponse().toString();

                System.out.println(response);

            } else {
                System.out.println("HTTP/1.0 400 Bad Request");
                response = "HTTP/1.0 400 Bad Request\r\n";
            }

            makePackets(response);

            slidingWindow = new SlidingWindow(packets,this);

            receiveAck();

            System.out.println("connection done!");

        } catch (IOException ex){
            ex.getStackTrace();
        } catch (InterruptedException ex){
            ex.getStackTrace();
        }
    }

    private void receiveAck() throws IOException{
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        while (true){
            buf.clear();
            channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            int packetNum = getPacketNum(resp.getSequenceNumber());
            if(resp.getType() == 4){
                allSent = true;

                slidingWindow.rcvPacket(packetNum);
                if(!slidingWindow.isAllSent())
                    slidingWindow.sendNextPackets();

                sendWindow.put(resp.getSequenceNumber(),true);
                if(responseAllSent())
                    break;
            }
        }
    }

    private int getPacketNum(long sequenceNumber){
        for(int i = 0; i < packets.size(); i++){
            if(packets.get(i).getSequenceNumber() == sequenceNumber)
                return  i;
        }
        return - 1;
    }

    private void makePackets(String response){

        if(response.length() < 1000){
            Packet dataPacket = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(sequenceNumber)
                    .setPortNumber(packet.getPeerPort())
                    .setPeerAddress(packet.getPeerAddress())
                    .setPayload(response.getBytes())
                    .create();

            packets.add(dataPacket);
            sendWindow.put(sequenceNumber,false);

            return;
        } else {
            String firstData = response.substring(0,1000);
            response = response.substring(1000);
            Packet firstPacket = new Packet.Builder()
                    .setType(5)
                    .setSequenceNumber(sequenceNumber)
                    .setPortNumber(packet.getPeerPort())
                    .setPeerAddress(packet.getPeerAddress())
                    .setPayload(firstData.getBytes())
                    .create();

            packets.add(firstPacket);
            sendWindow.put(sequenceNumber,false);

            sequenceNumber++;

            while (response.length() > 1000){
                String middleData = response.substring(0,1000);
                response = response.substring(1000);
                Packet middlePacket = new Packet.Builder()
                        .setType(6)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(packet.getPeerPort())
                        .setPeerAddress(packet.getPeerAddress())
                        .setPayload(middleData.getBytes())
                        .create();
                packets.add(middlePacket);

                sendWindow.put(sequenceNumber,false);

                sequenceNumber++;
            }

            Packet lastPacket = new Packet.Builder()
                    .setType(7)
                    .setSequenceNumber(sequenceNumber)
                    .setPortNumber(packet.getPeerPort())
                    .setPeerAddress(packet.getPeerAddress())
                    .setPayload(response.getBytes())
                    .create();
            packets.add(lastPacket);

            sendWindow.put(sequenceNumber,false);

            System.out.println("last sent!!");
        }
    }

    private void processRequest(){

        RequestProcesser requestProcesser = new RequestProcesser(this);
        Thread processRequestThread = new Thread(requestProcesser);
        processRequestThread.start();
    }

    public void sendSynAck() throws IOException{

        Packet synAck = new Packet.Builder()
                .setType(2)
                .setSequenceNumber(sequenceNumber)
                .setPortNumber(packet.getPeerPort())
                .setPeerAddress(packet.getPeerAddress())
                .setPayload(((ackNumber + 1) + "").getBytes())
                .create();


        channel.send(synAck.toBuffer(),router);
        sequenceNumber ++;
    }

    public boolean responseAllSent(){
        for (long key: sendWindow.keySet()){
            if(!sendWindow.get(key)){
                return false;
            }
        }
        return true;
    }

    public long getSequenceNumber(){
        return sequenceNumber;
    }

    public SocketAddress getRouterAddress(){
        return router;
    }


    public DatagramChannel getChannel(){
        return channel;
    }

    public Packet getPacket(){
        return packet;
    }

    public void setConnected(boolean connected){
        this.connected = connected;
    }

    public void setAllSent(boolean allSent){
        this.allSent = allSent;
    }

    public boolean getAllSent(){
        return allSent;
    }

    public long getStartNumber(){
        return startNumber;
    }

    public long getEndNumber(){
        return endNumber;
    }

    public TreeMap<Long,String> getRcvWindow(){
        return rcvWindow;
    }

    public void setStartNumber(long startNumber){
        this.startNumber = startNumber;
    }

    public void setEndNumber(long endNumber){
        this.endNumber = endNumber;
    }

    public HashMap<Long,Boolean> getSendWindow(){
        return sendWindow;
    }

}

