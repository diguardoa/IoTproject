package WebServer.WebServer;

import org.eclipse.californium.core.WebLink;



public class Resource extends Thread {
	public String resource_name;
	public ValueManager manager;
	
	private ServerSubscriber controller_IN;
	private int id;
	
	protected String resource_in_path;
	protected Container res_container;
	protected String rt;
	
	
	//Class constructor
	//Create a Containers for each Resource as child of a Room or a Patient according to the parent_container link
	//Once the container is created a CoapServer is assigned to the resource in order to manage the subscription on the 
	//corresponding resource on MN
	public Resource(WebLink link, String parent_container,int id) {
		this.id = id;
		
		manager = new ValueManager();
		manager.setId(this.id);
		
		int length;
		if(link.getURI().contains("Patients")) {
			length = ("DiViProject-in-cse/DiViProject-in-name/SmartHospitalization/Patients/Patient0/").length();
			manager.setPatient();
		} else {
			length = ("DiViProject-in-cse/DiViProject-in-name/SmartHospitalization/Rooms/Room0/").length();
			manager.setRoom();
		}
		resource_name = link.getURI().substring(length);
		
		manager.setResourceName(resource_name);
		
		res_container = DiVi_ADN_IN.createContainer(parent_container, resource_name);
		resource_in_path = parent_container + "/" + resource_name;

		manager.setInPath(resource_in_path);
		
		// Fai partire l'oggetto server coap che fa la subscription su IN
		controller_IN = new ServerSubscriber(resource_name + "_pat", ++WebServer.server_coap_port,manager);
		controller_IN.start();
		
		String resource_mn_path = resource_in_path.replace("in", "mn");
		try {
			sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager.setMnPath(resource_mn_path);
		DiVi_ADN_IN.createSubscription(resource_mn_path, "coap://127.0.0.1:"+ WebServer.server_coap_port +"/" + resource_name + "_pat",resource_name + "_monitor");
	
	}
}
