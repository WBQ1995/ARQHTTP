package Client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConcurrentWrite implements Runnable{

    public void run(){

        String[] args = {"post", "-v", "-f", "ClientTest.txt", "http://localhost:8008/new1.txt"};

        try {
            Client client = new Client();
            client.handShake();
            Thread.sleep(100);

            ArgsParser parser = new ArgsParser(args);
            Request request = parser.getRequest();

            client.makePackets(request.getRequest());
            client.sendRequest();
            client.receiveAck();

            System.out.println("Request has been sent!\n");
            client.increaseAckNumber();

            ResponseProcesser responseProcesser = new ResponseProcesser(client);
            Thread processResponseThread = new Thread(responseProcesser);
            processResponseThread.start();

            Thread.sleep(4000);

            String data = "";
            for (long key:client.getRcvWindow().keySet()) {
                data += client.getRcvWindow().get(key);
            }

            data = analyzeData(data,request);

            System.out.println(data);

            System.out.println("client done!");

        } catch (IOException ex){
            ex.getStackTrace();
        } catch (InterruptedException ex){
            ex.getStackTrace();
        }
    }

    private  String analyzeData(String data, Request request) throws IOException{

        String[] headerAndBody = null;
        String display = "";

        headerAndBody = data.split("\r\n\r\n");
        if(request.getIsV()){
            display += headerAndBody[0] + "\r\n\r\n";
        }
        if(headerAndBody.length == 2)
            display += headerAndBody[1] + "\r\n";

        String[] body = headerAndBody[0].split(" ");

        if(request.getWriteFile() && !headerAndBody[1].equals("")){
            writeFile(request.getFileName(),headerAndBody[1]);
            System.out.println("Body of the reponse is written to file: " + request.getFileName() + "\r\n");
        }
        return display;
    }

    private void writeFile(String fileName,String data) throws IOException {
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(data);
        writer.close();
    }
}
