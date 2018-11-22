package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import Packet.Packet;

public class httpc {

     private static SocketAddress routerAddress = new InetSocketAddress("localhost",3000);
     private static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8008);
     private static DatagramChannel channel;

     private static long sequenceNumber;

     public static void main(String[] args) throws IOException {

          channel = DatagramChannel.open();

          handShake();
          sendData();
     }

     private static void sendData() throws IOException{
          Packet data = new Packet.Builder()
                  .setType(0)
                  .setSequenceNumber(sequenceNumber)
                  .setPortNumber(serverAddress.getPort())
                  .setPeerAddress(serverAddress.getAddress())
                  .setPayload("Hello there".getBytes())
                  .create();
          channel.send(data.toBuffer(), routerAddress);
          sequenceNumber++;

          ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
          channel.receive(buf);
          buf.flip();
          Packet resp = Packet.fromBuffer(buf);

          String payload = new String(resp.getPayload(),UTF_8);

          System.out.println(payload);
     }

     private static void handShake() throws IOException{

          sequenceNumber = (long) (Math.random() * 1000);

          Packet syn = new Packet.Builder()
                  .setType(1)
                  .setSequenceNumber(sequenceNumber)
                  .setPortNumber(serverAddress.getPort())
                  .setPeerAddress(serverAddress.getAddress())
                  .setPayload("".getBytes())
                  .create();
          channel.send(syn.toBuffer(), routerAddress);
          sequenceNumber++;

          ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);

          while (true){
               buf.clear();
               channel.receive(buf);
               buf.flip();
               Packet synAck = Packet.fromBuffer(buf);
               buf.flip();
               if(synAck.getType() == 2 && synAck.getSequenceNumber() == sequenceNumber){

                    System.out.println("get synAck");

                    sequenceNumber ++;
                    serverAddress = new InetSocketAddress("localhost",synAck.getPeerPort());

                    Packet synAckAck = new Packet.Builder()
                            .setType(3)
                            .setSequenceNumber(sequenceNumber)
                            .setPortNumber(serverAddress.getPort())
                            .setPeerAddress(serverAddress.getAddress())
                            .setPayload("".getBytes())
                            .create();
                    channel.send(synAckAck.toBuffer(), routerAddress);
                    sequenceNumber ++;
                    break;
               }
          }
     }
}
