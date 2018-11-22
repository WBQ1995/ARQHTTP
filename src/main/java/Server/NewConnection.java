package Server;

import Packet.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import static java.nio.charset.StandardCharsets.UTF_8;

public class NewConnection implements Runnable {

    private  SocketAddress router = new InetSocketAddress("localhost",3000);
    private  DatagramChannel channel;

    private long sequenceNumber;
    private int port;
    private Packet packet;

    public NewConnection(Packet packet){
        this.packet = packet;
        port = 9000 + (int)(Math.random() * 1000);
        sequenceNumber = packet.getSequenceNumber() + 1;

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
            recSynAckAck();
            recData();
            sendData();
        } catch (IOException ex){
            ex.getStackTrace();
        }
    }

    private void sendSynAck() throws IOException{
        SendSynAck synAck = new SendSynAck(sequenceNumber,this);
        Thread synAckThread = new Thread(synAck);
        synAckThread.start();
    }

    private void recSynAckAck() throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

        while (true) {
            buffer.clear();
            channel.receive(buffer);
            buffer.flip();
            Packet synAckAck = Packet.fromBuffer(buffer);
            buffer.flip();
            if (synAckAck.getType() == 3 && synAckAck.getSequenceNumber() == sequenceNumber) {
                System.out.println("get synAckAck");
                sequenceNumber++;
                break;
            }
        }
    }

    private void recData() throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        while (true){
            buffer.clear();
            channel.receive(buffer);
            buffer.flip();
            Packet recData = Packet.fromBuffer(buffer);
            buffer.flip();
            if(recData.getSequenceNumber() == sequenceNumber) {
                String data = new String(recData.getPayload(), UTF_8);
                System.out.println(data);
                sequenceNumber ++;
                break;
            }
        }
    }

    private void sendData() throws IOException{
        String payload = new String("got it!".getBytes(), UTF_8);
        Packet data = new Packet.Builder()
                .setType(0)
                .setSequenceNumber(sequenceNumber)
                .setPortNumber(packet.getPeerPort())
                .setPeerAddress(packet.getPeerAddress())
                .setPayload(payload.getBytes())
                .create();
        channel.send(data.toBuffer(), router);
        sequenceNumber++;
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
}
