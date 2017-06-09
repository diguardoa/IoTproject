import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;

public class Resource extends Thread {
	public String resource_name;
	
	private int server_coap_port;
	private ServerSubscriber controller_IN;
	
	private int current_value;
	private int next_value;
	
	private String resource_mn_path;
	private Container res_container;
	private String uri_mote;
	private String rt;
	
	private boolean automatic_mode;
	
	private CoapResponse to_publish;
	private boolean ready_to_publish;
	private CoapClient observable;
	private CoapObserveRelation relation;
	
	public Resource(WebLink link, String parent_container,String uri_server) {
		
		uri_mote = uri_server;
		resource_name = link.getAttributes().getTitle();
		automatic_mode = true;
		
		// Create container (only if the resource is a sensor or an actuator)
		rt = link.getAttributes().getResourceTypes().get(0);

		System.out.println("created");
		res_container = DiVi_ADN.createContainer(parent_container, resource_name);		
		resource_mn_path = parent_container + "/" + resource_name;

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
		
		// Fai partire l'oggetto server coap che fa la subscription su IN
		server_coap_port = ++ProxyClient.COAP_PORT;	
		controller_IN = new ServerSubscriber(resource_name + "_pat", server_coap_port);
		controller_IN.start();

		// al posto di resource_mn_path ci vuole la resource_in_path del controller dal quale vuoi prendere i dati
		DiVi_ADN.createSubscription(resource_mn_path, "coap://127.0.0.1:"+ server_coap_port +"/" + resource_name + "_pat",resource_name + "_monitor");
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
		while(true) {
			observingStep();
			// look if the resource is in automatic mode
			automatic_mode = controller_IN.isAutomaticMode();
			
if (ProxyClient.debug)
	controller_IN.debugPrintContentStr();

			// if it isn't in automatic mode get value from IN
			if (!automatic_mode)
				//sendValue(controller_IN.getValue());
						
			try {
				currentThread();
				Thread.sleep(ProxyClient.T_resource);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public synchronized void setValue(int value) {
		if (automatic_mode)
			sendValue(value);
	}
	
	private synchronized void sendValue(int value) {
		if (current_value != value)
		{
			String message = "e=" + String.valueOf(value);
			CoapClient pclient = new CoapClient(uri_mote);
			CoapResponse post_response = pclient.post(message,MediaTypeRegistry.TEXT_PLAIN);
		}
	}
	
	private synchronized void setCurrentValue(int value) {
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
