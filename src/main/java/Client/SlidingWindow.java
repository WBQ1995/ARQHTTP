package Client;

import Packet.Packet;

import java.util.ArrayList;
import java.util.TreeMap;

public class SlidingWindow {

    private int base;
    private int end;
    private final int length = 4;

    private boolean allSent = false;

    private TreeMap<Integer,Boolean> window;
    private Client client;
    private ArrayList<Packet> packets;

    public SlidingWindow(ArrayList<Packet> packets, Client client){
        this.packets = packets;
        this.client = client;

        window = new TreeMap<>();

        if(packets.size() <= length){

            base = 0;
            end = packets.size() - 1;

            for (int i = 0; i < packets.size(); i ++){
                SendPacket sendPacket;
                Thread sendPacketThread;

                sendPacket = new SendPacket(packets.get(i),client);
                sendPacketThread = new Thread(sendPacket);
                sendPacketThread.start();
                client.getSendWindow().put(packets.get(i).getSequenceNumber(),false);
                window.put(i,false);
            }
            allSent = true;
        } else {
            base = 0;
            end = base + length - 1;
            for (int i = base; i <= end; i ++){
                SendPacket sendPacket;
                Thread sendPacketThread;

                sendPacket = new SendPacket(packets.get(i),client);
                sendPacketThread = new Thread(sendPacket);
                sendPacketThread.start();
                client.getSendWindow().put(packets.get(i).getSequenceNumber(),false);
                window.put(i,false);
            }
        }
    }

    public void sendNextPackets(){
        SendPacket sendPacket;
        Thread sendPacketThread;
        while(base <= packets.size() - 1 && window.get(base)){
            base++;
            if(end != packets.size() - 1){
                end ++;
                sendPacket = new SendPacket(packets.get(end),client);
                sendPacketThread = new Thread(sendPacket);
                sendPacketThread.start();
                client.getSendWindow().put(packets.get(end).getSequenceNumber(),false);
                window.put(end,false);
            } else {
                allSent = true;
            }
        }
    }

    public void rcvPacket(int packetNum){
        if(inWindow(packetNum))
            window.put(packetNum,true);
    }

    public boolean isAllSent(){
        return allSent;
    }

    private boolean inWindow(int packetNum){
        return (packetNum >= base && packetNum <= end);
    }
}
