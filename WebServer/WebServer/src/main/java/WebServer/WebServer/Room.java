package WebServer.WebServer;

import org.eclipse.californium.core.WebLink;

public class Room extends Hospital_Entity {
	public Resource AIRCON;
	public Resource FIREAL;
	public Resource TEMPR;
	public Resource TRS;
	
	
	public Room(int i, WebLink aircon, WebLink fireal, WebLink tempr, String parents_ct){
		id = i;
		DiVi_ADN_IN.createContainer(parents_ct, new String("Room" + id));
		my_container_long_name = parents_ct + "/Room"+id ;
		
		if(aircon != null)
			AIRCON = new Resource(aircon, my_container_long_name, id);
		if(fireal != null)
			FIREAL= new Resource(fireal, my_container_long_name, id);
		if(tempr != null)
			TEMPR = new Resource(tempr, my_container_long_name, id);
	}
	
	public ValueManager getManager(String res_name) {
		ValueManager request = null;

		switch (res_name) {
		case "AirCon":
			request = AIRCON.manager;
			break;
		case "FireAl":
			request = FIREAL.manager;
			break;
		case "TempR":
			request = TEMPR.manager;
			break;
		}

		return request;
	}
}
