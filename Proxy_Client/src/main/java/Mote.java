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
		
	}
}
