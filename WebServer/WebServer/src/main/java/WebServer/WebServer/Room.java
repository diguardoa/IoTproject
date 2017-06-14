package WebServer.WebServer;

import org.eclipse.californium.core.WebLink;
import org.json.JSONObject;

public class Room {
	public int id;
	private String my_container_long_name;
	private Resource AIRCON;
	private Resource FIREAL;
	private Resource TEMPR;
	private Resource TRS;
	
	
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

	public JSONObject getStatus() {
		JSONObject resp = new JSONObject();
		
		resp.put("id", 8);
		resp.put("desc", "GetLastStatus");
		resp.put("type", "r");
		resp.put("id_ent", id);
		resp.put("AirCon", AIRCON.manager.simpleGetLastValue());
		resp.put("FireAl", FIREAL.manager.simpleGetLastValue());
		resp.put("TempR", TEMPR.manager.simpleGetLastValue());
		
		return resp;
	}
}
