import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.json.JSONObject;

public class Mote {
	public URI uri;
	public CoapClient mote_c;
	
	public Mote(String string) {
		uri = null;
		try {
			uri = new URI(string);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}	
		mote_c = new CoapClient(uri);
		
		CoapClient client = new CoapClient(uri + "/.well-known/core/");
		client.setTimeout(0);
		CoapResponse response = client.get();
		String str = response.getResponseText();
		//System.out.println(str);
		String[] parts = str.split(",<");
		String[] resource_name = parts[1].split(">;");
		
		Set<WebLink> links = mote_c.discover();
		if(links!=null){
			for (WebLink link : links) {
				final String resUri = link.getURI();
				
				if (!resUri.equalsIgnoreCase("/.well-known/core")) {
					ResourceAttributes attr = link.getAttributes();
					if(attr.getCount()>1){
						Set<String> set_attr = attr.getAttributeKeySet();
						for(String s: set_attr)
							System.out.println(s + " " + link.getAttributes().getAttributeValues(s));
					}
						
					
					CoapClient client1 = new CoapClient(uri + resource_name[0]);
					client1.setTimeout(0);
					CoapResponse res = client1.get();
					//System.out.println(resource_name[0] + " -> " + res.getResponseText());
					//final String resID = link.getURI().replaceFirst("/", "");
					
					CoapClient c = new CoapClient(uri+link.getURI());
					c.setTimeout(0);
					CoapResponse resp = (c.get());
					//System.out.println(resp.getResponseText());
					
						//System.out.println("New Resource: " + resID + " (" + resUri + ")");
						/*ui.access(new Runnable() {
							public void run() {
								// Add mote
								Mote mote = getMote(moteID, proxyUri + resUri);
								if (mote != null) {
									ui.getMotes().addBean(mote);
									ui.getGrid().markAsDirtyRecursive();
									new Observer(moteID, proxyUri + resUri, ui);
									ui.getGoogleMap().addMarker(mote.getMarker());
									ui.push();
								} else {
									System.err.println("Error retriving mote information");
								}
							}
						});*/
				
				}
			}
		//CoapResponse response= mote_c.get();
    	
    	//System.out.println(new String(response.getPayload()));
	}
	}
}
