import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.UIDefaults.ProxyLazyValue;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;



public class DiVi_ADN extends Thread{
	private URI uri;
	private List<String> addresses = new LinkedList<>();
	private List<Patient> patients = new LinkedList<>();
	private List<Room> rooms = new LinkedList<>();
	private AE SmartHospital;
	private Container Patients;
	private Container Rooms;
	
	static public RoomsAlarm general_alarm;
	
	/*
	 * DiViADN publishes on the MN
	 */
	
	public DiVi_ADN(String br_uri) {
		
		uri = DiVi_ADN.createUri(br_uri);
		
		// Create the application entity
		
		SmartHospital = DiVi_ADN.createAE(
				ProxyClient.MN_address, 
				"SmartHospitalization");
	
		// Create Patients Container
		
		Patients = DiVi_ADN.createContainer(
				ProxyClient.MN_address + "/DiViProject-mn-name/SmartHospitalization", 
				"Patients");
		
		// Create Rooms Container
		
		Rooms = DiVi_ADN.createContainer(
				ProxyClient.MN_address + "/DiViProject-mn-name/SmartHospitalization", 
				"Rooms");
			
		// Create the Alarm
		general_alarm = new RoomsAlarm();
	}


	public void discovery() {
		List<String> motes_add;
		
		// Get all motes address
		
		motes_add = getNodeAddress();


		// Gets all resources

		for (String r: motes_add) 
			getResources(r);
			
	}
	
	public void start_pat_rooms() {
		for (Patient pat : patients) 
			pat.start();

		for (Room rom: rooms)
			rom.start();
	}
	
	private void getResources(String add) {
		URI uri_mote = DiVi_ADN.createUri(add);	
		CoapClient mote_c = new CoapClient(uri_mote);
		mote_c.setTimeout(0);	// infinite timeout
		
		// Wait 
		try {
			Thread.sleep(ProxyClient.delay_get_resources);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		Set<WebLink> links = mote_c.discover();	
	
		
		if(links!=null){
			
			
			 // Look if it is a Patient or a Room sensor

			CoapClient info_mote = new CoapClient(DiVi_ADN.createUri(uri_mote + "/id"));
			mote_c.setTimeout(0);	// infinite timeout
			
			//Wait 
			try {
				Thread.sleep(ProxyClient.delay_get_resources);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			CoapResponse info_mote_resp = info_mote.get();
			
			
			if (info_mote_resp != null) {
				JSONObject jsonOBJ = new JSONObject(info_mote_resp.getResponseText());
				
				if (jsonOBJ.getString("type").compareTo("pat") == 0 )
					getPatientResource(links, jsonOBJ.getInt("id"), uri_mote);
				else if (jsonOBJ.getString("type").compareTo("room") == 0 )
					getRoomResource(links, jsonOBJ.getInt("id"), uri_mote);
			}
		} else
			System.out.println(add + " not found");
	}
	
	private void getRoomResource(Set<WebLink> res_set, int room_id, URI uri_mote) {
		Room current_room;
		
		// Look if the room exist. If it doesn't create it
		List<Room> look_for_room = rooms.stream().filter(a -> Objects.equals(a.seqNumber, room_id)).collect(Collectors.toList());
		if (look_for_room.isEmpty())
		{
			current_room = new Room(room_id);
			//current_room.start();
			rooms.add(current_room);
		}
		else
			current_room = look_for_room.get(0);
		
		for (WebLink link : res_set) {
			final String resUri = link.getURI();	
			if (!resUri.equalsIgnoreCase("/.well-known/core") && !resUri.equalsIgnoreCase("/id"))
				current_room.addResource(link, uri_mote + resUri);
			
		}
		
	}
	private void getPatientResource(Set<WebLink> res_set, int pat_id, URI uri_mote) {
		Patient current_pat;
		
		// Look for the patient, if it exist. If it is not create it
		List<Patient> look_for_patient = patients.stream()
				.filter(a -> Objects.equals(a.seqNumber, pat_id))
				.collect(Collectors.toList());
		
		if (look_for_patient.isEmpty())
		{
			current_pat = new Patient(pat_id);
			//current_pat.start();
			patients.add(current_pat);
		}
		else
			current_pat = look_for_patient.get(0);

		for (WebLink link : res_set) {
			final String resUri = link.getURI();	
			if (!resUri.equalsIgnoreCase("/.well-known/core") && !resUri.equalsIgnoreCase("/id"))
				current_pat.addResource(link, uri_mote + resUri);
			
		}
	}
	
	
	private List<String> getNodeAddress() {
		
		HttpClient client = new DefaultHttpClient();
				
		HttpUriRequest request = new HttpGet(uri);
		System.out.println(uri);
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String temp = EntityUtils.toString(response.getEntity());
			String[] parts = temp.split("</pre>Routes<pre>");			
			
			parts = parts[1].split("</pre></body></html>");
			
			// divido per righe
			String[] rows = parts[0].split(Pattern.quote("\n"));
			for (String row: rows) 
			{
				String[] first_part_row = row.split("/128");
				addresses.add("coap://[" + first_part_row[0] + "]:5683");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Print all the addresses
		for (String row: addresses)
			System.out.println(row);
		return addresses;
		
	}
	
	public void run() {
		
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
if (ProxyClient.oM2M_active) {
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
if (ProxyClient.debug)
	System.out.println(body);

		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
if (ProxyClient.debug)
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
} else
		return null;
	}
	
	static Container createContainer(String cse, String rn){
if (ProxyClient.oM2M_active) {

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
		
if (ProxyClient.debug)
	System.out.println(body);
		
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		
		String response = new String(responseBody.getPayload());
		
if (ProxyClient.debug)
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
} else
	return null;
	}
	
	static void createContentInstance(String cse, String cnf, String con){
if (ProxyClient.oM2M_active) {
		
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
if (ProxyClient.debug) {
	System.out.println(uri);
	System.out.println(body);
}
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		
		String response = new String(responseBody.getPayload());
if (ProxyClient.debug)
	System.out.println(response);
} 			
	}
	
	static String Discovery(String cse) {
if (ProxyClient.oM2M_active) {

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
} else
	return null;
	}

	static void createSubscription(String cse, String notificationUrl, String nameSub){
if (ProxyClient.oM2M_active) {
		CoapClient client = new CoapClient(cse);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 23));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("rn", nameSub);
		content.put("nu", notificationUrl);
		content.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", content);
		String body = root.toString();
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
//		String response = new String(responseBody.getPayload());
//		System.out.println(response);
		
		/*JSONObject content = new JSONObject();
		content.put("rn", "Monitor");
		content.put("nu", notificationUrl);
		content.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", content);
		String body = root.toString();
		try {
			System.out.println(Request.Post(cse)
					.addHeader("X-M2M-Origin", "admin:admin")
					.bodyString(body, ContentType.APPLICATION_JSON)
					.setHeader("Content-Type", "application/json;ty=23")
					.execute().returnContent().asString());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
}		
	}

}
