package WebServer.WebServer;

import org.json.JSONArray;
import org.json.JSONObject;

public class ValueManager {
	private String type;
	private String rn;
	private String in_path;
	private JSONArray values_db;
	private int id;
	
	public ValueManager() {
		values_db = new JSONArray();
	}
	
	public void setPatient() {
		type = "p";
	}
	
	public void setRoom() {
		type = "r";
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setResourceName(String nm) {
		rn = nm;
	}
	
	public void setInPath(String in_path) {
		this.in_path = in_path;
	}
	
	public synchronized void addValue(JSONObject value) {
		values_db.put(value);
		
		// prova
		// SetValue(0);
	}
	
	public synchronized void printValues() {
		System.out.println(values_db);
	}
	
	
	public synchronized JSONObject getAllValues() {
		JSONObject resp = new JSONObject();
		
		resp.put("id", 3);
		resp.put("desc", "GetAllValues");
		resp.put("type", type);
		resp.put("id_ent", id);
		resp.put("res_name", rn);
		resp.put("payload", values_db);
		
		return resp;
	}
	
	public synchronized JSONObject deleteValues() {
		JSONObject resp = new JSONObject();

		values_db = new JSONArray();
		
		resp.put("id", 4);
		resp.put("desc", "DeleteValues");
		resp.put("type", type);
		resp.put("id_ent", id);
		resp.put("res_name", rn);
		resp.put("payload", "done");
		
		return resp;
	}
	
	public synchronized JSONObject setValue(int val) {
		JSONObject resp = new JSONObject();
		
		DiVi_ADN_IN.createContentInstance(in_path, "", String.valueOf(val));
		
		resp.put("id", 5);
		resp.put("desc", "SetValue");
		resp.put("type", type);
		resp.put("id_ent", id);
		resp.put("res_name", rn);
		resp.put("payload", "done");
		
		return resp;
	}
	
	public synchronized JSONObject setAutomaticMode() {
		JSONObject resp = new JSONObject();
		
		DiVi_ADN_IN.createContentInstance(in_path, "", String.valueOf(-1));
		
		resp.put("id", 6);
		resp.put("desc", "SetAutomaticMode");
		resp.put("type", type);
		resp.put("id_ent", id);
		resp.put("res_name", rn);
		resp.put("payload", "done");
		
		return resp;
	}
	
	public synchronized JSONObject getLastValue() {
		JSONObject resp = new JSONObject();
		
		resp.put("id", 7);
		resp.put("desc", "GetLastValue");
		resp.put("type", type);
		resp.put("id_ent", id);
		resp.put("res_name", rn);
		resp.put("payload", values_db.get(values_db.length()));
		
		return resp;		
	}

}
