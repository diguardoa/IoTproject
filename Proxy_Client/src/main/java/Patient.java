import java.net.URI;
import java.util.List;
import java.util.Set;

import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.server.resources.ResourceAttributes;

public class Patient {
	public int seqNumber;
	public int resNumber;
	private List<Resource> resources;
	
	
	public Patient(int id) {
		seqNumber = id;
		resNumber = 0;
		/*
		 * Crea runnable che parte ritardato e se non trova risorse si elimina 
		 */
	}
	
	public void addResource(URI uri, WebLink link) {
		System.out.println(uri);
		
		/*
		 * Guarda se la risorsa esiste già, se no creala
		 */
		
		ResourceAttributes attr = link.getAttributes();
		if(attr.getCount()>1){
			Set<String> set_attr = attr.getAttributeKeySet();
			for(String s: set_attr)
				System.out.println(s + " " + link.getAttributes().getAttributeValues(s));
		}	
			
	}
}
