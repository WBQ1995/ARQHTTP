package Server;

import java.io.IOException;

public class httpfs {
    public static void main(String[] args){
        Server server = new Server();
        try {
            server.startServer();
        } catch (IOException ex){
            ex.getStackTrace();
        }
    }
}
