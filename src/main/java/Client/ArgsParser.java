package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class ArgsParser {
	
	private ArrayList<String> args;
	private	Request request;
	private Boolean setConnection;
	
	public ArgsParser(){}
	public ArgsParser(String[] args){
		this.args = new ArrayList<String>();
		for(String arg : args){
			System.out.print(arg + " ");
			this.args.add(arg);
		}
		System.out.println("\r\n");
		request = new Request();
		setConnection = true;
		
		if (args[0].equals("help")) {
			request.setMethod(Method.HELP);
			help();
			setConnection = false;
		} else if(args[0].equals("get") || args[0].equals("post")){
			processGetAndPost();
		} else {
			setConnection = false;
		}
		if(!setConnection){
			if(request.getMethod().equals(Method.HELP)){
				System.exit(0);
			}
			printError();
			System.exit(0);
		}
	}
	
	public Request getRequest(){
		return request;
	}
	
	private void processGetAndPost(){
		String url;
		String subUrl;
		String host;
		String path;
		int port;
		int firstSlashIndex = -1;
		
		if(args.get(0).equals("get")){
			request.setMethod(Method.GET);
		} else {
			request.setMethod(Method.POST);
		}
		args.remove(0);
		
		if(args.contains("-o")){
			int indexOfO = args.indexOf("-o");
			if(indexOfO == args.size() - 1){
				setConnection = false;
				return;
			}
			String outPutFlie = args.get(indexOfO + 1);
			args.remove(indexOfO);
			args.remove(indexOfO);
			request.setWriteFile(true);
			request.setFileName(outPutFlie);
		}
		
		 url = args.get(args.size() - 1);
		
		if(args.get(args.size() - 1).trim().length() < 8 || 
				!args.get(args.size() - 1).trim().substring(0,7).equals("http://")){
			setConnection = false;
			return;
		}
		args.remove(args.size() - 1);
		
		if(args.contains("-v")){
			int index = args.indexOf("-v");
			request.setV(true);
			args.remove(index);
		}
		
		if(request.getMethod().equals(Method.POST)){
			if(args.contains("-d") && args.contains("-f")){
				setConnection = false;
				return;
			}
			if(args.contains("-d")){
				int index = args.indexOf("-d");
				if(index != args.size() - 2){
					setConnection = false;
					return;
				}
				request.setData(args.get(index + 1));
				args.remove(args.size() - 1);
				args.remove(args.size() - 1);
			} 
			if(args.contains("-f")){
				int index = args.indexOf("-f");
				if(index != args.size() - 2){
					setConnection = false;
					return;
				}
				try {
					request.setData(readFile(args.get(index + 1)));
				} catch (IOException e) {
					e.printStackTrace();
				}
				args.remove(args.size() - 1);
				args.remove(args.size() - 1);
			}
		}
		
		if(args.contains("-h")){
			int index = args.indexOf("-h");
			request.setH(true);
			args.remove(index);
			for(String arg : args){
				if(!arg.contains(":")){
					setConnection = false;
					return;
				}
				String[] header = arg.split(":");
				request.setHeader(header[0], header[1]);
			}
		}
		
		request.setUrl(url);
		subUrl = url.substring(7);
		firstSlashIndex = subUrl.indexOf('/');
		
		String hostAndPort = subUrl.substring(0,firstSlashIndex);
		if (hostAndPort.contains(":")) {
			int colIndex = hostAndPort.indexOf(':');
			host = hostAndPort.substring(0,colIndex);
			port = Integer.parseInt(hostAndPort.substring(colIndex + 1));
		} else {
			host = subUrl.substring(0,firstSlashIndex);
			port = 80;
		}
		path = subUrl.substring(firstSlashIndex);
		request.setHost(host);
		request.setPath(path);
		request.setPort(port);
	}
	
	private void help(){
		if(args.size() == 1){
			printHelp();
		} else if(args.size() == 2 && args.get(1).equals("get")){
			printHelpGet();
		} else if(args.size() == 2 && args.get(1).equals("post")){
			printHelpPost();
		} else{
			printError();
		}
	}
	
	private void printHelp(){
		System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
		System.out.println("Usage:");
		System.out.println("    httpc command [arguments]");
		System.out.println("The commands are:");
		System.out.println("    get     executes a HTTP GET request and prints the response.");
		System.out.println("    post    executes a HTTP POST request and prints the response.");
		System.out.println("    help    prints this screen.\n");
		System.out.println("Use \"httpc help [command]\" for more information about a command.");
	}
	private void printHelpGet(){
		System.out.println("usage: httpc get [-v] [-h key:value] URL\n");
		System.out.println("Get executes a HTTP GET request for a given URL.\n");
		System.out.println("   -v             Prints the detail of the response such as protocol, status, and headers.");
		System.out.println("   -h key:value   Associates headers to HTTP Request with the format 'key:value'.");
	}
	private void printHelpPost(){
		System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n");
		System.out.println("Post executes a HTTP POST request for a given URL with inline data or from file.\n");
		System.out.println("   -v             Prints the detail of the response such as protocol, status, and headers.");
		System.out.println("   -h key:value   Associates headers to HTTP Request with the format 'key:value'.");
		System.out.println("   -d string      Associates an inline data to the body HTTP POST request.");
		System.out.println("   -f file        Associates the content of a file to the body HTTP POST request.\n");
		System.out.println("Either [-d] or [-f] can be used but not both.");
	}
	private void printError(){
		System.out.println("Invalid curl.");
	}

	private String readFile(String fileName) throws IOException{
		File file = new File(fileName);
        FileReader reader = new FileReader(file);
        BufferedReader bReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String data = "";
        while ((data =bReader.readLine()) != null) {
            sb.append(data);
        }
        bReader.close();
        data= sb.toString();
        return data;
	}
}
