import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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


// DiVi ADN publish on MN oM2M node

public class DiVi_ADN {
	private URI uri;
	private List<String> addresses = new LinkedList<>();
	private List<Patient> patients = new LinkedList<>();
	private List<Room> rooms = new LinkedList<>();
	
	static public RoomsAlarm general_alarm;
	
	/*
	 * The function registers the ADN on MN and create the main containers
	 */
	public DiVi_ADN(String br_uri) {
		
		uri = DiVi_ADN.createUri(br_uri);
		
		// Create the application entity
		
		DiVi_ADN.createAE(
				ProxyClient.MN_address, 
				"SmartHospitalization");
	
		// Create Patients Container
		
		DiVi_ADN.createContainer(
				ProxyClient.MN_address + "/DiViProject-mn-name/SmartHospitalization", 
				"Patients");
		
		// Create Rooms Container
		
		DiVi_ADN.createContainer(
				ProxyClient.MN_address + "/DiViProject-mn-name/SmartHospitalization", 
				"Rooms");
			
		// Create the Alarm
		general_alarm = new RoomsAlarm();
	}


	/*
	 * The function performs the Discovery procedure
	 */
	public void discovery() {
		List<String> motes_add;
		
		// Get all motes address
		
		motes_add = getNodeAddress();


		// Gets all resources and register them in the MN

		for (String r: motes_add) 
			getResources(r);
			
	}
	
	/*
	 * The function starts all threads related with Patients and Rooms control
	 */
	public void start_pat_rooms() {
		for (Patient pat : patients) 
			pat.start();

		for (Room rom: rooms)
			rom.start();
	}
	
	/*
	 * the function gets and register on MN all resources starting
	 * from their IPv6 mode address
	 */
	
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
		
		/*
		 *  Discovery process on the Mote: get all resources with their
		 *  attributes (title, rt, obs flag)
		 */
		
		Set<WebLink> links = mote_c.discover();	
	
		
		if(links!=null){
			
			/*
			 * Gets information about resource class from /id resource
			 * (all motes have /id resource)
			 */

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
				
				/*
				 * Distinguish between patient and room resources
				 */
				if (jsonOBJ.getString("type").compareTo("pat") == 0 )
					getPatientResource(links, jsonOBJ.getInt("id"), uri_mote);
				else if (jsonOBJ.getString("type").compareTo("room") == 0 )
					getRoomResource(links, jsonOBJ.getInt("id"), uri_mote);
			}
		} else
			System.out.println(add + " not found");
	}
	
	/*
	 * Configure resources belonging to room class
	 */
	private void getRoomResource(Set<WebLink> res_set, int room_id, URI uri_mote) {
		Room current_room;
		
		// Look if the room exist. If it doesn't create it
		List<Room> look_for_room = rooms.stream().filter(a -> Objects.equals(a.seqNumber, room_id)).collect(Collectors.toList());
		if (look_for_room.isEmpty())
		{
			current_room = new Room(room_id);
			rooms.add(current_room);
		}
		else
			current_room = look_for_room.get(0);
		
		// assign the resource to the correct room
		for (WebLink link : res_set) {
			final String resUri = link.getURI();	
			if (!resUri.equalsIgnoreCase("/.well-known/core") && !resUri.equalsIgnoreCase("/id"))
				current_room.addResource(link, uri_mote + resUri);
			
		}
		
	}
	
	/*
	 * Configure resources belonging to patient class
	 */
	private void getPatientResource(Set<WebLink> res_set, int pat_id, URI uri_mote) {
		Patient current_pat;
		
		// Look for the patient, if it exist. If it is not create it
		List<Patient> look_for_patient = patients.stream()
				.filter(a -> Objects.equals(a.seqNumber, pat_id))
				.collect(Collectors.toList());
		
		if (look_for_patient.isEmpty())
		{
			current_pat = new Patient(pat_id);
			patients.add(current_pat);
		}
		else
			current_pat = look_for_patient.get(0);

		// assign the resource to the correct patient
		for (WebLink link : res_set) {
			final String resUri = link.getURI();	
			if (!resUri.equalsIgnoreCase("/.well-known/core") && !resUri.equalsIgnoreCase("/id"))
				current_pat.addResource(link, uri_mote + resUri);
			
		}
	}
	
	
	/*
	 * The function performs an HTTP/Get to the BR router. Response contains 
	 * all motes that belongs to its DODAG
	 */
	private List<String> getNodeAddress() {
		
		HttpClient client = new DefaultHttpClient();
				
		// Perform the get
		HttpUriRequest request = new HttpGet(uri);
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Extract addresses from the response
		try {
			String temp = EntityUtils.toString(response.getEntity());
			String[] parts = temp.split("</pre>Routes<pre>");			
			
			parts = parts[1].split("</pre></body></html>");
			
			String[] rows = parts[0].split(Pattern.quote("\n"));
			for (String row: rows) 
			{
				String[] first_part_row = row.split("/128");
				addresses.add("coap://[" + first_part_row[0] + "]:5683");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Print all the addresses
		for (String row: addresses)
			System.out.println(row);
		return addresses;
		
	}
	
	/*
	 * This function manages exception on URI creation 
	 * (first version of the project, now quite useless)
	 */
	static URI createUri(String uri_string) {
		URI uri_created = null;
		try {
			uri_created = new URI(uri_string);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return uri_created;
	}
	
	/*
	 * STATIC METHODS
	 * They characterize application behavior as ADN on oM2M service infrastructure. 
	 */
	
	/*
	 * This function registers the ADN on MN (creation of the AE)
	 * If the AE is already registered, do nothing
	 */
	static void createAE(String cse, String rn){
if (ProxyClient.oM2M_active) {
	
		if (isAlreadyCreated(cse+ "/DiViProject-mn-name/"+ rn,2))
				return;
		
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


		
		return;
} else
		return;
	}
	
	/*
	 * This function exploit RESTful interface of MN to delete resourcese. 
	 * (in particular it is used to delete content instance)
	 */
	static void delete(String cse){
		URI uri = createUri(cse);
		CoapClient client = new CoapClient(uri);
		Request req = Request.newDelete();
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		CoapResponse responseBody = client.advanced(req);
	}
	
	/*
	 * The function is used to create container.
	 * 	rn: new container name
	 * 	cse: ablosute hierarchical position in which I want to create the container.
	 * If the container already exists, do nothing.
	 */
	static void createContainer(String cse, String rn){
		
if (ProxyClient.oM2M_active) {

		if (isAlreadyCreated(cse + "/" + rn,3))
			return;
	
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

		
		return;
} else
	return;
	}
	
	/*
	 * The function is used to create a content instance.
	 * 	cnf: content instance info ('cnf' field)
	 *  con: content instance content ('con' field)
	 * 	cse: ablosute hierarchical position in which I want to create the Content instance.
	 * If the CI already exists, do nothing.
	 */
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
	
	/*
	 * The function return true if cse resource already exist.
	 * 'type' specify the resource you're looking for
	 * (remember: AE=2, CONT=4 ...)
	 */
	static boolean isAlreadyCreated(String cse, int type) {
		String st_ae = DiVi_ADN.oM2Mdiscovery(cse + "?fu=1&rty=" + type);
		if (st_ae == null)
			return false;
		else
			return true;
	}
	
	/*
	 * The function perform a COAP/Get on the address specified on cse (it can
	 * contains query). It results in a oM2M/Discovery iff cse string represent
	 * a well formed discovery query 
	 */
	static String oM2Mdiscovery(String cse) {
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
		if (response.equals("Resource not found") || response.contains("Resource not found"))
			return null;
		JSONObject content = new JSONObject(response);
		String path = content.getString("m2m:uril");
		return path;
} else
	return null;
	}

	/*
	 * The function create a subscription. It may generate errors if the 
	 * subscription already exists. 
	 * 	nameSub: name of the subscription
	 *  notificationUrl: self explained
	 *  cse: absolute path in which the subscription has to be created.
	 */
	
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

}		
	}

}
