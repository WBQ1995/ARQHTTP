package Server;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;

public class Processor {

    //private String request;
    private String path;
    private Request request;
    private Response response;
    private Boolean overwrite = true;

    public Processor(Request request,String path){
        response = new Response();
        this.request = request;
        if(path.equals("/")){
            this.path = System.getProperty("user.dir");
        } else {
            this.path = path;
        }
    }

    public Response getResponse(){

        if(!request.getVersion().equals("HTTP/1.0") && !request.getVersion().equals("HTTP/1.1")){
            response.setState("505 HTTP Version Not Supported");
            return response;
        }

        if(request.isGet()) {
            processGet();
        } else {
            processPost();
        }
        return response;
    }

    private void processPost(){

        path += request.getFilePath();
        try {
            FileWriter fileWriter = new FileWriter(path,overwrite);
            fileWriter.write(request.getBody());
            fileWriter.close();
            response.setState("200 OK");
        } catch (IOException ex){
            response.setState("403 Forbidden");
        }
    }

    private void processGet(){

        response.setState("200 OK");

        if(request.getFilePath().equals("/")){
            showAllFiles();
            return;
        }
        path += request.getFilePath();

        try {
            readFile();
        } catch (FileNotFoundException ex ){
            response.setState("404 Not Found");
            return;
        } catch (IOException ex){
            response.setState("400 Bad Request");
            return;
        }
    }

    private void showAllFiles(){
        File file = new File(path);
        File[] filesList = file.listFiles();
        String body = "";
        if (filesList.length == 0){
            return;
        }
        for (File files : filesList){
            if(files.isDirectory())
                body += files.getName() + "/" + "\n";
            else
                body += files.getName() + "\n";
        }
        response.setBody(body);
    }

    private void readFile() throws FileNotFoundException, IOException {
        File file = new File(path);
        FileReader reader = new FileReader(file);
        BufferedReader bReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = bReader.readLine()) != null) {
            sb.append(line + "\r\n");
        }
        bReader.close();
        response.setBody(sb.toString());
        response.setHeader("Content-Type", new MimetypesFileTypeMap().getContentType(file));
        if(file.getName().endsWith("txt")){
            response.setHeader("Content-Disposition","attachment");
        } else if(file.getName().endsWith("html")){
            response.setHeader("Content-Disposition","inline");
        }
    }
}
