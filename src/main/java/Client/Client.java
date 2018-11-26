package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
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

     private HashMap<Long,Boolean> window;


     public Client() throws IOException{
          channel = DatagramChannel.open();
          sequenceNumber = (long) (Math.random() * 1000);
          window = new HashMap<>();
     }

     public void sendData(String data) {

         int c = 1;

         Packet dataPacket = new Packet.Builder()
                 .setType(4)
                 .setSequenceNumber(sequenceNumber)
                 .setPortNumber(serverAddress.getPort())
                 .setPeerAddress(serverAddress.getAddress())
                 .setPayload(((ackNumber + 1 + "") + Integer.toString(c) + "aaa ").getBytes())
                 .create();

         window.put(sequenceNumber,false);

//         String payload = new String(dataPacket.getPayload(), StandardCharsets.UTF_8);
//         System.out.println(payload);

         SendPacket sendPacket = new SendPacket(dataPacket,this);
         Thread sendPacketThread = new Thread(sendPacket);
         sendPacketThread.start();



         for (int i = 0; i < 40; i ++) {
             sequenceNumber++;
             c++;
             Packet dataPacket1 = new Packet.Builder()
                     .setType(5)
                     .setSequenceNumber(sequenceNumber)
                     .setPortNumber(serverAddress.getPort())
                     .setPeerAddress(serverAddress.getAddress())
                     .setPayload((Integer.toString(c) + "aaa ").getBytes())
                     .create();
             window.put(sequenceNumber,false);
//             payload = new String(dataPacket1.getPayload(), StandardCharsets.UTF_8);
//             System.out.println(payload);
             SendPacket sendPacket1 = new SendPacket(dataPacket1,this);
             Thread sendPacketThread1 = new Thread(sendPacket1);
             sendPacketThread1.start();
         }

         sequenceNumber++;
         c++;
         Packet dataPacket2 = new Packet.Builder()
                 .setType(6)
                 .setSequenceNumber(sequenceNumber)
                 .setPortNumber(serverAddress.getPort())
                 .setPeerAddress(serverAddress.getAddress())
                 .setPayload((Integer.toString(c) + "aaa ").getBytes())
                 .create();

         window.put(sequenceNumber,false);

//         payload = new String(dataPacket2.getPayload(), StandardCharsets.UTF_8);
//         System.out.println(payload);
         SendPacket sendPacket2 = new SendPacket(dataPacket2,this);
         Thread sendPacketThread2 = new Thread(sendPacket2);
         sendPacketThread2.start();

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

     public boolean allSent(){
         for (long key: window.keySet()){
             if(!window.get(key)){
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

     public long getAckNumber(){
         return ackNumber;
     }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean getConnected(){
         return connected;
    }

    public HashMap<Long,Boolean> getWindow(){
         return window;
    }
}
