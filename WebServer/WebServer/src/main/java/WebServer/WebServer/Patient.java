package WebServer.WebServer;

import org.eclipse.californium.core.WebLink;

public class Patient {
	public int id;
	public Resource HRS;
	public Resource LA;
	public Resource OXYVALV;
	public Resource TEMP;
	public Resource OXYS;
	
	private String my_container_long_name;
	
	public Patient(int i, WebLink hrs, WebLink la, WebLink oxyvalv, WebLink temp, WebLink oxys, String parents_ct){
		id = i;
		DiVi_ADN_IN.createContainer(parents_ct, new String("Patient" + id));
		my_container_long_name = parents_ct + "/Patient"+id ;
		
		if(hrs != null)
			HRS = new Resource(hrs, my_container_long_name, null);
		if(la != null)
			LA = new Resource(la, my_container_long_name, null);
		if(oxyvalv != null)
			OXYVALV = new Resource(oxyvalv, my_container_long_name, null);
		if(temp != null)
			TEMP = new Resource(temp, my_container_long_name, null);
		if(oxys != null)	
			OXYS = new Resource(oxys, my_container_long_name, null);
		
	}
	
	public String getData(Resource r){
		return r.resource_name;
	}

}
