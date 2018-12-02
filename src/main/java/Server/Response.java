package Server;

import java.util.HashMap;

public class Response {
    private final String version = "HTTP/1.0";
    private String state = "";
    private HashMap<String,String> headers;
    private String body;

    public Response() {
        headers = new HashMap<>();
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setHeader(String front,String end) {
        headers.put(front,end);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String toString(){
        String response = version + " " + state + "\r\n";

        for (String key : headers.keySet()){
            response += key + ": " + headers.get(key) + "\r\n";
        }
        response += "\r\n";

        if(body != null)
            response += body;
        return response;
    }

}
