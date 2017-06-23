import java.net.SocketException;

import org.json.JSONObject;


/*
 * This class aims to create and manage subscription on IN controller
 */
public class ServerSubscriber extends Thread{
	private String resource_name;
	private String resource_in_path;
	private boolean automaticMode;
	private int port_number;
	private int currentValue;
	private CoAPMonitor server;

	/*
	 * The constructor accept
	 * 	resource_name: resource 'title' attribute
	 * 	port_number: port number of local CoAP server that waits for notification 
	 * 	resource_in_path: IN address in which subscription has to be created
	 */
	
	public ServerSubscriber(String resource_name, int port_number, String resource_in_path) {
		this.resource_name = resource_name;
		this.port_number = port_number;
		this.resource_in_path = resource_in_path;
		automaticMode = true;
		currentValue = -1;	// -1 stays for automaticMode
	}
	
	/*
	 * Thread periodically look for IN controller Container until it is found.
	 * Then a subscription is created and the thread ends 
	 */
	public void run() {
		
		// Create the CoAP Monitor on the given local port
		try {
			server = new CoAPMonitor(resource_name,port_number);
			server.addEndpoints();
	    	server.start();
	    	
		} catch (SocketException e) {
			e.printStackTrace();
		}
		

		// Wait until the IN controller path exists
		while (!DiVi_ADN.isAlreadyCreated(resource_in_path, 3)) {
			try {
				Thread.sleep(ProxyClient.delay_subscription_IN);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// create the subscription
		DiVi_ADN.createSubscription(resource_in_path, "coap://127.0.0.1:"+ port_number +"/" + resource_name,resource_name + "_monitor");

		// notify the creation
		System.out.println("observing " + resource_in_path );
	}
	
	/*
	 * This function gets a value from IN Controller (if it already exists)
	 * Returns true if Remote User has set 'automaticMode'
	 * ('automaticMode' is default true)
	 */
	
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
		else
			automaticMode = false;
		
		return automaticMode;
	}
	
	public void debugPrintContentStr() {
		
		System.out.println(getValue());
	}
	
	/*
	 * This function returns the last value notified from IN controller.
	 * It is equal to -1 if nothing has already been notified.
	 * 
	 * IT MUST BE INVOKED AFTER isAutomaticMode() METHOD
	 */
	public int getValue() {
		
		return currentValue;
	}
}
