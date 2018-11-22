package Client;

import java.io.IOException;

public class httpc {

    public static void main(String[] args) throws InterruptedException{
        try {
            Client client = new Client();
            client.handShake();
            Thread.sleep(100);
            client.sendData();
        } catch (IOException ex){
            ex.getStackTrace();
        }
    }
}
