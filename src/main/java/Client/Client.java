package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import Packet.Packet;

public class Client {

    private SocketAddress routerAddress =
            new InetSocketAddress("localhost",3000);

    private InetSocketAddress serverAddress =
            new InetSocketAddress("localhost", 8008);

    private DatagramChannel channel;

    private long sequenceNumber;

    private long ackNumber = -1;

    private boolean connected = false;

    private HashMap<Long,Boolean> sendWindow;

    private boolean allReceived = false;

    private boolean allSent = false;

    private long startNumber = -1;
    private long endNumber = -1;

    private TreeMap<Long,String> rcvWindow;

    private ArrayList<Packet> packets;

    private SlidingWindow slidingWindow;

    public Client() throws IOException{
          channel = DatagramChannel.open();
          sequenceNumber = (long) (Math.random() * 1000);
          sendWindow = new HashMap<>();
          rcvWindow = new TreeMap<>();

          packets = new ArrayList<Packet>();

     }

    public void receiveAck() throws IOException{
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
                if(requestAllSent())
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

    public void sendRequest(){
        slidingWindow = new SlidingWindow(packets,this);
    }

    public void makePackets(String request) {

         if(request.length() <= 1000){
             Packet dataPacket = new Packet.Builder()
                     .setType(0)
                     .setSequenceNumber(sequenceNumber)
                     .setPortNumber(serverAddress.getPort())
                     .setPeerAddress(serverAddress.getAddress())
                     .setPayload(request.getBytes())
                     .create();

             packets.add(dataPacket);
             sendWindow.put(sequenceNumber,false);
             return;
         } else {

             String firstData = request.substring(0, 1000);
             request = request.substring(1000);
             Packet firstPacket = new Packet.Builder()
                     .setType(5)
                     .setSequenceNumber(sequenceNumber)
                     .setPortNumber(serverAddress.getPort())
                     .setPeerAddress(serverAddress.getAddress())
                     .setPayload(firstData.getBytes())
                     .create();

             packets.add(firstPacket);
             sendWindow.put(sequenceNumber, false);

             sequenceNumber++;

             while (request.length() > 1000) {
                 String middleData = request.substring(0, 1000);
                 request = request.substring(1000);
                 Packet middlePacket = new Packet.Builder()
                         .setType(6)
                         .setSequenceNumber(sequenceNumber)
                         .setPortNumber(serverAddress.getPort())
                         .setPeerAddress(serverAddress.getAddress())
                         .setPayload(middleData.getBytes())
                         .create();

                 packets.add(middlePacket);
                 sendWindow.put(sequenceNumber, false);

                 sequenceNumber++;
             }

             Packet lastPacket = new Packet.Builder()
                     .setType(7)
                     .setSequenceNumber(sequenceNumber)
                     .setPortNumber(serverAddress.getPort())
                     .setPeerAddress(serverAddress.getAddress())
                     .setPayload(request.getBytes())
                     .create();
             packets.add(lastPacket);

             sendWindow.put(sequenceNumber, false);

             System.out.println("last sent!!");
         }
    }

    public void handShake() throws IOException{

        sendSyn();

        recSynAck();

        sendSynAckAck();

    }

    private void sendSyn(){

        SendSyn syn = new SendSyn(this);
        Thread synThread = new Thread(syn);
        synThread.start();
    }

    private void recSynAck() throws IOException{
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        while (true){
            buf.clear();
            channel.receive(buf);
            buf.flip();
            Packet synAck = Packet.fromBuffer(buf);
            buf.flip();
            String payload = new String(synAck.getPayload(), StandardCharsets.UTF_8);

            long ackFromServer = Long.parseLong(payload);

            if(synAck.getType() == 2 && ackFromServer == sequenceNumber){
                System.out.println("get synAck");
                ackNumber = synAck.getSequenceNumber();
                serverAddress =
                        new InetSocketAddress("localhost",synAck.getPeerPort());
                break;
               }
          }
     }

    private void sendSynAckAck(){

        SendSynAckAck synAckAck = new SendSynAckAck(this);
        Thread synAckAckThread = new Thread(synAckAck);
        synAckAckThread.start();
    }

    public boolean requestAllSent(){
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
        return routerAddress;
    }

    public InetSocketAddress getServerAddress(){
        return serverAddress;
    }

    public DatagramChannel getChannel(){
        return channel;
    }

    public void increaseSequenceNumber(){
        sequenceNumber ++;
    }

    public long getAckNumber(){ return ackNumber; }

    public void increaseAckNumber(){ackNumber ++;}



    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean getConnected(){
         return connected;
    }

    public HashMap<Long,Boolean> getSendWindow(){
         return sendWindow;
    }

    public void setAllReceived(boolean allReceived) { this.allReceived = allReceived; }

    public boolean getAllReceived() {return allReceived; }

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

}
