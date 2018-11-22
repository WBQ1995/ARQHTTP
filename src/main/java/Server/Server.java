package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import Packet.Packet;


public class Server {

    private int port;

    public Server(){
        port = 8008;
    }

    public void startServer() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(port));
        System.out.println("Server listening...");

        ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

        while (true) {
            buffer.clear();
            channel.receive(buffer);
            buffer.flip();
            Packet packet = Packet.fromBuffer(buffer);
            buffer.flip();

            if(packet.getType() == 1){
                System.out.println("get syn");
                NewConnection connection = new NewConnection(packet);
                Thread thread = new Thread(connection);
                thread.start();
            }
        }
    }

}
