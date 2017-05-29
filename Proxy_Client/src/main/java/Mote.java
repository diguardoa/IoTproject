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
		Set<WebLink> links = mote_c.discover();
		
		if(links!=null){
			for (WebLink link : links) {
				final String resUri = link.getURI();
				String l = link.toString();
				System.out.println(link.getAttributes().getAttributeValues("i"));
				if (!resUri.equalsIgnoreCase("/.well-known/core")) {
					final String resID = link.getURI().replaceFirst("/", "");
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
