import java.net.SocketException;

import org.json.JSONObject;

public class ServerSubscriber extends Thread{
	private String nm_server_resource;
	private boolean automaticMode;
	private int port_number;
	private int currentValue;
	private CoAPMonitor server;

	
	public ServerSubscriber(String nm_server_resource, int port_number) {
		this.nm_server_resource = nm_server_resource;
		this.port_number = port_number;
		automaticMode = true;
		currentValue = -1;
	}
	
	public void run() {
		try {
			server = new CoAPMonitor(nm_server_resource,port_number);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isAutomaticMode() {
		String temp = server.getContentStr();
		if (temp != null)
		{
			JSONObject root = new JSONObject(temp);
			JSONObject m2msgn = (JSONObject) root.get("m2m:sgn");
			JSONObject nev = (JSONObject) m2msgn.get("nev");
			JSONObject rep = (JSONObject) nev.get("rep");
			currentValue = rep.getInt("con");
		} 
		if (currentValue < 0)
			automaticMode = true;
		
		return automaticMode;
	}
	
	public void debugPrintContentStr() {
		
		System.out.println(getValue());
	}
	
	public int getValue() {
		
		return currentValue;
	}
}
