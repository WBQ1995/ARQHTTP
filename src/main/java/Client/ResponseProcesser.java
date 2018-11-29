package Client;

import Packet.Packet;
import Server.RequestHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ResponseProcesser implements Runnable {

    private Client client;
    public ResponseProcesser(Client client){
        this.client = client;
    }

    public void run(){

        ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

        while (!client.getAllReceived()){
            buffer.clear();

            try {
                client.getChannel().receive(buffer);
                buffer.flip();
                Packet rcvPacket = Packet.fromBuffer(buffer);

                ResponseHandler responseHandler = new ResponseHandler(rcvPacket,this.client);
                Thread t = new Thread(responseHandler);
                t.start();
            } catch (IOException ex){
                ex.getStackTrace();
            }
        }
    }

}
