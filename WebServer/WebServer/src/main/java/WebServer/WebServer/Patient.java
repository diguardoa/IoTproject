package WebServer.WebServer;

import org.eclipse.californium.core.WebLink;
import org.json.JSONObject;

public class Patient {
	public int id;
	private String my_container_long_name;
	private Resource HRS;
	private Resource LA;
	private Resource OXYVALV;
	private Resource TEMP;
	private Resource OXYS;
	
	
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

	public JSONObject SetAutomaticModeAll() {
		JSONObject resp = new JSONObject();
		
		resp.put("id", 9);
		resp.put("desc", "SetAutomaticModeAll");
		resp.put("type", "p");
		resp.put("id_ent", id);
		HRS.manager.setAutomaticMode();
		LA.manager.setAutomaticMode();
		OXYVALV.manager.setAutomaticMode();
		TEMP.manager.setAutomaticMode();
		OXYS.manager.setAutomaticMode();
		
		return resp;
	}
	
	public JSONObject getStatus() {
		JSONObject resp = new JSONObject();
		
		resp.put("id", 8);
		resp.put("desc", "GetLastStatus");
		resp.put("type", "p");
		resp.put("id_ent", id);
		resp.put("HRS", HRS.manager.simpleGetLastValue());
		resp.put("LedA", LA.manager.simpleGetLastValue());
		resp.put("OxyValv", OXYVALV.manager.simpleGetLastValue());
		resp.put("Temp", TEMP.manager.simpleGetLastValue());
		resp.put("OxyS", OXYS.manager.simpleGetLastValue());
		
		return resp;
	}

}
