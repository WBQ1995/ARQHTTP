package Client;

import Packet.Packet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;

public class httpc {

    public static void main(String[] args) throws InterruptedException{
        try {
            Client client = new Client();
            client.handShake();
            Thread.sleep(100);

            File myFile = new File("/Users/WBQ/IdeaProjects/ARQHTTP/src/main/java/ClientTest_1.txt");
            FileReader fileReader = new FileReader(myFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String request = "";
            String line = null;
            while ((line = reader.readLine()) != null){
                request += line;
            }
            reader.close();

            client.makePackets(request);
            client.sendRequest();
            client.receiveAck();

            System.out.println("Request has been sent!");
            client.increaseAckNumber();

            ResponseProcesser responseProcesser = new ResponseProcesser(client);
            Thread processResponseThread = new Thread(responseProcesser);
            processResponseThread.start();

            Thread.sleep(2000);

            String data = "";
            for (long key:client.getRcvWindow().keySet()) {
                data += client.getRcvWindow().get(key);
            }
            System.out.println(data);

            System.out.println("client done!");

        } catch (IOException ex){
            ex.getStackTrace();
        }
    }
}
