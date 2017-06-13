package WebServer.WebServer;

import java.net.SocketException;

import org.json.JSONArray;

public class ServerSubscriber extends Thread{
	private String nm_server_resource;
	private int port_number;
	private ValueManager manager;
	
	public ServerSubscriber(String nm_server_resource, int port_number, ValueManager manager) {
		this.nm_server_resource = nm_server_resource;
		this.port_number = port_number;
		this.manager = manager;
	}
	
	public void run() {
		CoAPMonitor server;
		try {
			server = new CoAPMonitor(nm_server_resource,port_number,manager);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}