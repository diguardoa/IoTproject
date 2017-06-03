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



public class DiVi_ADN {
	public URI uri;
	public List<String> addresses = new LinkedList<>();
	public List<Patient> patients = new LinkedList<>();
	public AE SmartHospital;
	
	public DiVi_ADN(String br_uri) {
		uri = createUri(br_uri);
		
		SmartHospital = createAE("coap://127.0.0.1:5684/~/DiViProject-mn-cse", "SmartHospitalization");
		//Container container = createContainer("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/TempApp", "DATA");
		//createContentInstance("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/TempApp/DATA");
	}
	
	private URI createUri(String uri_string) {
		URI uri_created = null;
		try {
			uri_created = new URI(uri_string);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return uri_created;
	}

	public void discovery() {
		List<String> motes_add;
		
		/*
		 * Gets all motes address
		 */
		
		motes_add = getNodeAddress();
		
		/*
		 * Gets all resources
		 */
		
		for (String r: motes_add)
			 getResources(r);
		
		/*
		 * Look if there are patient without resources. if yes delete them
		 */
		
		
	}
	
	public void getResources(String add) {
		URI uri_mote = createUri(add);	
		CoapClient mote_c = new CoapClient(uri_mote);	
		Set<WebLink> links = mote_c.discover();
		
		if(links!=null){
			
			/*
			 * Look if it is a Patient or a Room sensor
			 */
			CoapClient info_mote = new CoapClient(createUri(uri_mote + "/id"));
			CoapResponse info_mote_resp = info_mote.get();
			
			if (info_mote_resp != null) {
				JSONObject jsonOBJ = new JSONObject(info_mote_resp.getResponseText());
				
				if (jsonOBJ.getString("type").compareTo("pat") == 0 )
					getPatientResource(links, jsonOBJ.getInt("id"), uri_mote);
				else if (jsonOBJ.getString("type").compareTo("room") == 0 )
					getRoomResource(links, jsonOBJ.getInt("id"), uri_mote);
			}
		}
	}
	public void getRoomResource(Set<WebLink> res_set, int room_id, URI uri_mote) {
	
	}
	public void getPatientResource(Set<WebLink> res_set, int pat_id, URI uri_mote) {
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

		for (WebLink link : res_set) {
			final String resUri = link.getURI();	
			if (!resUri.equalsIgnoreCase("/.well-known/core") && !resUri.equalsIgnoreCase("/id"))
				current_pat.addResource(createUri(uri_mote + resUri), link);
			
		}
	}
	
	
	public List<String> getNodeAddress() {
		
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
		return addresses;
		
	}
	
	public AE createAE(String cse, String rn){
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
}
