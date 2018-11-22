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

        try{
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(port));
        } catch (IOException ex){
            ex.getStackTrace();
        }
    }

    public void run(){
        sequenceNumber = packet.getSequenceNumber() + 1;

        Packet synAck = new Packet.Builder()
                .setType(2)
                .setSequenceNumber(sequenceNumber)
                .setPortNumber(packet.getPeerPort())
                .setPeerAddress(packet.getPeerAddress())
                .setPayload("".getBytes())
                .create();
        try {
            channel.send(synAck.toBuffer(), router);
            sequenceNumber++;


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

            buffer.clear();
            channel.receive(buffer);
            buffer.flip();
            Packet recData = Packet.fromBuffer(buffer);
            buffer.flip();
            String data = new String(recData.getPayload(),UTF_8);
            System.out.println(data);

            if (recData.getSequenceNumber() == sequenceNumber) {
                sequenceNumber++;

                String payload = new String("got it!".getBytes(), UTF_8);

                Packet resData = new Packet.Builder()
                        .setType(0)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(packet.getPeerPort())
                        .setPeerAddress(packet.getPeerAddress())
                        .setPayload(payload.getBytes())
                        .create();
                channel.send(resData.toBuffer(), router);
                sequenceNumber++;
            }
        } catch (IOException ex){
            ex.getStackTrace();
        }
    }
}
