package WebServer.WebServer;

import org.eclipse.californium.core.WebLink;

public class Patient extends Hospital_Entity{
	public Resource HRS;
	public Resource LA;
	public Resource OXYVALV;
	public Resource TEMP;
	public Resource OXYS;
	
	
	public Patient(int i, WebLink hrs, WebLink la, WebLink oxyvalv, WebLink temp, WebLink oxys, String parents_ct){
		id = i;
		DiVi_ADN_IN.createContainer(parents_ct, new String("Patient" + id));
		my_container_long_name = parents_ct + "/Patient"+id ;
		
		if(hrs != null)
			HRS = new Resource(hrs, my_container_long_name, id);
		if(la != null)
			LA = new Resource(la, my_container_long_name, id);
		if(oxyvalv != null)
			OXYVALV = new Resource(oxyvalv, my_container_long_name, id);
		if(temp != null)
			TEMP = new Resource(temp, my_container_long_name, id);
		if(oxys != null)	
			OXYS = new Resource(oxys, my_container_long_name, id);
		
	}

	public ValueManager getManager(String res_name) {
		ValueManager request = null;

		switch (res_name) {
		case "HRS":
			request = HRS.manager;
			break;
		case "LedA":
			request = LA.manager;
			break;
		case "OxyValv":
			request = OXYVALV.manager;
			break;
		case "Temp":
			request = TEMP.manager;
			break;
		case "OxyS":
			request = OXYS.manager;
			break;
		}

		return request;
	}

}
