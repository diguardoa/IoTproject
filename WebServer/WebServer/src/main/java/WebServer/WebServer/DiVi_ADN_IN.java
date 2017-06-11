package WebServer.WebServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

public class DiVi_ADN_IN extends Thread{
	
	public AE SmartHospital;
	public Container Patients;
	public Container Rooms;
	
	public LinkedList<String> findContainer() {
		System.out.println("Search for AE");
		String st_ae = discovery("coap://127.0.0.1:5683/~/DiViProject-mn-cse?fu=1&rty=2");
		
		SmartHospital = DiVi_ADN_IN.createAE(
				"coap://127.0.0.1:5683/~/DiViProject-in-cse", 
				"SmartHospitalization");
		//System.out.println(st_ae);
		System.out.println("Search for Container");
		String str_ae = "coap://127.0.0.1:5683/~" + st_ae + "?fu=1&rty=3";
		String str_cont = discovery(str_ae);
		String[] containers = str_cont.split(" /");
		LinkedList<String> ll = new LinkedList<String>(Arrays.asList(containers));
		ll.addFirst(st_ae);
		
		return ll;
	}
	
	public LinkedList<String> findPatientRoom(LinkedList<String> ll, String str){
		int x = 0, index = -1;
		LinkedList<String> container = new LinkedList<String>();
		for(String s: ll){
			if(s.equals(str)){
				index = x;
				if(ll.get(index+1).toString().equals(new String(str + "/Patient1"))){					
					int y = index+1;
					while(y < ll.size() && ll.get(y).toString().contains(new String(str + "/Patient1"))){
						container.add(ll.get(y));					
						y++;
					}
				}
				if(ll.get(index+1).toString().equals(new String(str + "/Room1"))){
					int y = index+1;
					while(y < ll.size() && ll.get(y).toString().contains(new String(str + "/Room1"))){
						container.add(ll.get(y));					
						y++;
					}
				}
			}
			x++;
		}
		return container;
	}
	
	static String discovery(String cse){
		CoapClient client = new CoapClient(cse);
		Request req = Request.newGet();
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
		JSONObject content = new JSONObject(response);
		String path = content.getString("m2m:uril");
		return path;
	}
	
	static void createSubscription(String cse, String notificationUrl, String nameSubscription){
		CoapClient client = new CoapClient(cse);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 23));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("rn", nameSubscription);
		content.put("nu", notificationUrl);
		content.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", content);
		String body = root.toString();
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
		System.out.println(response);
		
	}
	
	static URI createUri(String uri_string) {
		URI uri_created = null;
		try {
			uri_created = new URI(uri_string);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return uri_created;
	}
	
	static AE createAE(String cse, String rn){
		AE ae = new AE();
		URI uri = createUri(cse);
		CoapClient client = new CoapClient(uri);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 2));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("api",rn + "-ID");
		obj.put("rr","true");
		obj.put("rn", rn);
		JSONObject root = new JSONObject();
		root.put("m2m:ae", obj);
		String body = root.toString();
		System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
		if(response.contains("already"))
			return null;
		System.out.println(response);
		JSONObject resp = new JSONObject(response);
		JSONObject container = (JSONObject) resp.get("m2m:ae");
		ae.setRn((String) container.get("rn"));
		ae.setTy((Integer) container.get("ty"));
		ae.setRi((String) container.get("ri"));
		ae.setPi((String) container.get("pi"));
		ae.setCt((String) container.get("ct"));
		ae.setLt((String) container.get("lt"));
		
		return ae;
	}
	
	static Container createContainer(String cse, String rn){
		Container container = new Container();

		URI uri = createUri(cse);
		CoapClient client = new CoapClient(uri);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 3));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("rn", rn);
		JSONObject root = new JSONObject();
		root.put("m2m:cnt", obj);
		String body = root.toString();
		System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		
		String response = new String(responseBody.getPayload());
		if(response.contains("already"))
			return null;
		System.out.println(response);
		
		JSONObject resp = new JSONObject(response);
		JSONObject cont = (JSONObject) resp.get("m2m:cnt");
		container.setRn((String) cont.get("rn"));
		container.setTy((Integer) cont.get("ty"));
		container.setRi((String) cont.get("ri"));
		container.setPi((String) cont.get("pi"));
		container.setCt((String) cont.get("ct"));
		container.setLt((String) cont.get("lt"));
		container.setSt((Integer) cont.get("st"));
		container.setOl((String) cont.get("ol"));
		container.setLa((String) cont.get("la"));
		
		return container;
	}
	static void createContentInstance(String cse, String cnf, String con){
		
		URI uri = createUri(cse);
		CoapClient client = new CoapClient(uri);
		//client.setTimeout(0);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 4));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("cnf",cnf); // Content Info
		content.put("con",con);	// Data
		JSONObject root = new JSONObject();
		root.put("m2m:cin", content);
		String body = root.toString();
		System.out.println(uri);
		System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		
		String response = new String(responseBody.getPayload());
		System.out.println(response);
			
	}
	
	static String Discovery(String cse) {
		URI uri = null;
		try {
			uri = new URI(cse);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CoapClient client = new CoapClient(uri);
		Request req = Request.newGet();
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
		JSONObject content = new JSONObject(response);
		String path = content.getString("m2m:uril");
		return path;
	}

}
