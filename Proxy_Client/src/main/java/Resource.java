import java.net.URI;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.json.JSONObject;

public class Resource extends Thread {
	public String resource_name;
	
	protected int current_value;
	protected int next_value;
	
	protected String resource_mn_path;
	protected Container res_container;
	protected String uri_mote;
	protected String rt;
	
	private CoapResponse to_publish;
	private boolean ready_to_publish;
	private CoapClient observable;
	private CoapObserveRelation relation;
	
	public Resource(WebLink link, String parent_container,String uri_server) {
		
		uri_mote = uri_server;
		resource_name = link.getAttributes().getTitle();

		// Create container (only if the resource is a sensor or an actuator)
		rt = link.getAttributes().getResourceTypes().get(0);

		if (rt.equals("S") || rt.equals("A")) {
			System.out.println("created");
			res_container = DiVi_ADN.createContainer(parent_container, resource_name);		
			resource_mn_path = parent_container + "/" + resource_name;
		}
		// Look if the resource is observable
		if (link.getAttributes().containsAttribute("obs")) {
			System.out.println("the resource is observable");
			
			ready_to_publish = false;
			
			observable = new CoapClient(uri_mote);
			relation = observable.observe(new CoapHandler() {
				@Override
				public void onLoad(final CoapResponse curr_response) 
				{
					if (curr_response != null) {
						to_publish = curr_response;
						ready_to_publish = true;
					} else
						System.out.println("null response");
				}
				
				@Override
				public void onError() {
					System.out.println("Error!");				
				}
				
			});
		} else {
			System.out.println("the resource is non observable");
		}
		
	}
	
	public void observingStep() {

		if (ready_to_publish==true) {
			// All msg are conformed with SenML standard
			JSONObject jsonOBJ = new JSONObject(to_publish.getResponseText());
			String value = jsonOBJ.get("e").toString();
			setCurrentValue(Integer.parseInt(value));
			String mes_unity = jsonOBJ.get("u").toString();
			DiVi_ADN.createContentInstance(resource_mn_path, mes_unity, value);
			ready_to_publish = false;
		}
	}
	
	public void run() {
		if (rt.equals("A") || rt.equals("S"))
			observingStep();
		try {
			currentThread();
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setValue(int value) {
		String message = "e=" + String.valueOf(value);
		CoapClient pclient = new CoapClient(uri_mote);
		CoapResponse post_response = pclient.post(message,MediaTypeRegistry.TEXT_PLAIN);
	}
	
	public synchronized void setCurrentValue(int value) {
		current_value = value;
	}
	
	public synchronized int getValue() {
		return current_value;
	}
	
	public void cancel(){
		relation.proactiveCancel();
	}
	
	public boolean isCanceled(){
		return relation.isCanceled();
	}
}
