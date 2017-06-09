import java.net.SocketException;

public class ServerSubscriber extends Thread{
	private String nm_server_resource;
	private int port_number;
	
	public ServerSubscriber(String nm_server_resource, int port_number) {
		this.nm_server_resource = nm_server_resource;
		this.port_number = port_number;
	}
	
	public void run() {
		CoAPMonitor server;
		try {
			server = new CoAPMonitor(nm_server_resource,port_number);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
