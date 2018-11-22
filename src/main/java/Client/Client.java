package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import Packet.Packet;

public class Client {

     private SocketAddress routerAddress =
             new InetSocketAddress("localhost",3000);

     private InetSocketAddress serverAddress =
             new InetSocketAddress("localhost", 8008);

     private DatagramChannel channel;

     private long sequenceNumber;


     public Client() throws IOException{
          channel = DatagramChannel.open();
          sequenceNumber = (long) (Math.random() * 1000);
     }

     public void sendData(Packet data) throws IOException{

         SendPacket sendPacket = new SendPacket(data,this,sequenceNumber);
         Thread sendPacketThread = new Thread(sendPacket);
         sendPacketThread.start();

     }

     public void handShake() throws IOException{

          sendSyn();

          recSynAck();

          sendSynAckAck();

     }

     private void sendSyn() throws IOException{

          SendSyn syn = new SendSyn(sequenceNumber,this);
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
               if(synAck.getType() == 2 && synAck.getSequenceNumber() == sequenceNumber){

                    System.out.println("get synAck");
                    sequenceNumber ++;
                    serverAddress =
                            new InetSocketAddress("localhost",synAck.getPeerPort());
                    break;
               }
          }
     }

     private void sendSynAckAck() throws IOException{

          SendSynAckAck synAckAck = new SendSynAckAck(sequenceNumber,this);
          Thread synAckAckThread = new Thread(synAckAck);
          synAckAckThread.start();
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
}
