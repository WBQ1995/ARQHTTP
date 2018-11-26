package Server;

import Packet.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NewConnection implements Runnable {

    private  SocketAddress router = new InetSocketAddress("localhost",3000);
    private  DatagramChannel channel;

    private long sequenceNumber;
    private long ackNumber;
    private Packet packet;

    private boolean connected = false;

    private String rcvData = "";

    private boolean fin = false;

    private long startNumber = -1;
    private long endNumber = -1;
    private TreeMap<Long,String> window;

    public boolean allRecieved = false;

    public NewConnection(Packet packet){
        this.packet = packet;
        int port = 9000 + (int)(Math.random() * 1000);

        ackNumber = packet.getSequenceNumber();
        sequenceNumber = (long) (Math.random() * 1000);

        window = new TreeMap<>();

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

            Thread.sleep(2000);

            for (long key:window.keySet()){
                data += window.get(key);
            }

            System.out.println(data);

        } catch (IOException ex){
            ex.getStackTrace();
        } catch (InterruptedException ex){
            ex.printStackTrace();
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

    public void increaseSequenceNumber(){
        sequenceNumber ++;
    }

    public boolean getConnected(){
        return connected;
    }

    public void setConnected(boolean connected){
        this.connected = connected;
    }

    public void setRcvData(String data){
        this.rcvData = data;
    }

    public String getRcvData(){
        return rcvData;
    }

    public void setFin(boolean fin){
        this.fin = fin;
    }

    public boolean getFin(){
        return fin;
    }

    public long getStartNumber(){
        return startNumber;
    }

    public long getEndNumber(){
        return endNumber;
    }

    public TreeMap<Long,String> getWindow(){
        return window;
    }

    public void setStartNumber(long startNumber){
        this.startNumber = startNumber;
    }

    public void setEndNumber(long endNumber){
        this.endNumber = endNumber;
    }

}

