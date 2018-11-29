package Server;

import Packet.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RequestProcesser implements Runnable{

    private NewConnection connection;

    public RequestProcesser(NewConnection connection){
        this.connection = connection;
    }

    public void run(){
        ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
        while (!connection.getAllSent()){
            buffer.clear();

            try {
                connection.getChannel().receive(buffer);
                buffer.flip();
                Packet rcvPacket = Packet.fromBuffer(buffer);
                RequestHandler requestHandler = new RequestHandler(rcvPacket,this.connection);
                Thread t = new Thread(requestHandler);
                t.start();
            } catch (IOException ex){
                ex.getStackTrace();
            }
        }
    }
}
