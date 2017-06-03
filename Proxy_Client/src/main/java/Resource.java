import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.json.JSONObject;

// poi estenderà thread
public class Resource {
	public String resource_name;
	private Container res_container;
	
	public Resource(WebLink link, String parent_container) {
		

		resource_name = link.getAttributes().getTitle();
		JSONObject jsonOBJ = new JSONObject(link);
		//System.out.println(resource_name);

		// Create container
		res_container = ADN.createContainer(parent_container, resource_name);		

		// Create Instance (Class exercise)
		ADN.createContentInstance(parent_container + "/" + resource_name);
		
		/*
		ResourceAttributes attr = link.getAttributes();
		if(attr.getCount()>1){
			Set<String> set_attr = attr.getAttributeKeySet();
			for(String s: set_attr)
				System.out.println(s + " " + link.getAttributes().getAttributeValues(s));
		}	
		*/
	}
}
