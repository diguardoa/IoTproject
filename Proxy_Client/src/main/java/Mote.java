import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.Response;

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
		Set<WebLink> response = mote_c.discover();
		if (response != null)
			System.out.println(response.toString());
		//CoapResponse response= mote_c.get();
    	
    	//System.out.println(new String(response.getPayload()));
		
	}





}
