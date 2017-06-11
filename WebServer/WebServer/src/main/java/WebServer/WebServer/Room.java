package WebServer.WebServer;

import org.eclipse.californium.core.WebLink;

public class Room {
	public int id;
	public Resource AIRCON;
	public Resource FIREAL;
	public Resource TEMPR;
	public Resource TRS;
	
	private String my_container_long_name;
	
	//public Room(int i, WebLink aircon, WebLink fireal, WebLink te, WebLink trs, String parents_ct){
	public Room(int i, WebLink aircon, WebLink fireal, WebLink tempr, String parents_ct){
		id = i;
		DiVi_ADN_IN.createContainer(parents_ct, new String("Room" + id));
		my_container_long_name = parents_ct + "/Room"+id ;
		
		if(aircon != null)
			AIRCON = new Resource(aircon, my_container_long_name, null);
		if(fireal != null)
			FIREAL= new Resource(fireal, my_container_long_name, null);
		if(tempr != null)
			TEMPR = new Resource(tempr, my_container_long_name, null);
		//if(trs != null)
			//TRS = new Resource(trs, my_container_long_name, null);
	}
}
