import java.net.SocketException;

import org.json.JSONObject;

public class ServerSubscriber extends Thread{
	private String nm_server_resource;
	private String resource_in_path;
	private boolean automaticMode;
	private int port_number;
	private int currentValue;
	private CoAPMonitor server;

	
	public ServerSubscriber(String nm_server_resource, int port_number, String resource_in_path) {
		this.nm_server_resource = nm_server_resource;
		this.port_number = port_number;
		this.resource_in_path = resource_in_path;
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
		

		
		while (!DiVi_ADN.isAlreadyCreated(resource_in_path, 3)) {
			try {
				Thread.sleep(ProxyClient.delay_subscription_IN);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DiVi_ADN.createSubscription(resource_in_path, "coap://127.0.0.1:"+ port_number +"/" + nm_server_resource,nm_server_resource + "_monitor");

		System.out.println("observing " + resource_in_path );
	}
	
	public boolean isAutomaticMode() {	
		Thread.yield();
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
