import java.net.URI;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.json.JSONObject;

public class Resource {
	public String resource_name;
	
	private String resource_mn_path;
	private Container res_container;
	private Observer observer;
	private String uri_mote;
	
	public Resource(WebLink link, String parent_container,String uri_server) {
		
		uri_mote = uri_server;
		resource_name = link.getAttributes().getTitle();

		// Create container
		res_container = ADN.createContainer(parent_container, resource_name);		
		resource_mn_path = parent_container + "/" + resource_name;
		
		// Look if the resource is observable
		if (link.getAttributes().containsAttribute("obs")) {
			System.out.println("the resource is observable");	
			observer = new Observer(uri_mote,resource_mn_path);
			observer.start();
		} else {
			System.out.println("the resource is non observable");
		}
		
	}
}
