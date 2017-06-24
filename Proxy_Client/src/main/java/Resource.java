import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;

/*
 * Resource class perform the proxy functionalities of ADN. There are many
 * resource as the number of motes on 6LowPAN network.
 */
public class Resource extends Thread {
	public String resource_name;
	
	private int server_coap_port;
	private ServerSubscriber controller_IN;
	
	private int current_value;
	
	private String resource_mn_path;
	private String uri_mote;
	private String rt;
	
	private boolean automatic_mode;
	
	private CoapResponse to_publish;
	private boolean ready_to_publish;
	private CoapClient observable;
	private CoapObserveRelation relation;
	
	/*
	 * The constructor creates a new resource with
	 * 	link: WebLink resulting from a CoAP/Discovery, it is used to get
	 * 		resource attributes
	 * 	parent_container: the container on the patient/room to which the resource belongs (on MN side)
	 * 	uri_server: Address of CoAP resource on 6LowPAN network
	 * 
	 */
	
	public Resource(WebLink link, String parent_container,String uri_server) {
		
		uri_mote = uri_server;
		resource_name = link.getAttributes().getTitle();
		automatic_mode = true;
		current_value = -1;
		
		// Create container (only if the resource is a sensor or an actuator)
		rt = link.getAttributes().getResourceTypes().get(0);

		System.out.println("created");
		DiVi_ADN.createContainer(parent_container, resource_name);		
		resource_mn_path = parent_container + "/" + resource_name;

		// Look if the resource is observable
		if (link.getAttributes().containsAttribute("obs")) {
			System.out.println("the resource is observable");
			
			ready_to_publish = false;		
			observable = new CoapClient(uri_mote);	
			
			/*
			 * Create a CoAP/Observe relation
			 */
			relation = observable.observe(new CoapHandler() {
				@Override
				public void onLoad(final CoapResponse curr_response) 
				{
					/*
					 * Whether a notification arrives from 6LowPAN mote, it set a flag and put the message
					 * on a buffer. Resource.run() will proxying the message
					 */
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
	
	/*
	 * Proxy Step. Notification from 6LowPAN are published ad ContentInstance on MN 
	 */
	public void observingStep(){

		if (ready_to_publish==true) {
			try {
				// All msg are conformed with SenML standard
				JSONObject jsonOBJ = new JSONObject(to_publish.getResponseText());
				String value = jsonOBJ.get("e").toString();
				int temp_value = Integer.parseInt(value);

				setCurrentValue(temp_value);			
				String mes_unity = jsonOBJ.get("u").toString();
				DiVi_ADN.createContentInstance(resource_mn_path, mes_unity, value);
			
			} catch (Exception e) {
				System.out.println("error in observing step");
			}
			
			ready_to_publish = false;
		}
	}
	
	/*
	 * The periodic thread 
	 * 	- subscribe to the controller counterpart on IN (once)
	 *  - perform observing step
	 *  - acquire CI from IN controller (if they are present) and performe
	 *  	6LowPAN resource status change according with requirements
	 */
	
	public void run() {
		
		// Assign a different server coap port for each resource
		server_coap_port = ++ProxyClient.COAP_PORT;	

		// Subscribe to IN controller
		controller_IN = new ServerSubscriber(resource_name, 
				server_coap_port,resource_mn_path.replaceAll("mn", "in"));
		
		// Start observing IN controller 
		controller_IN.start();
		
		try {
			Thread.sleep(ProxyClient.delay_subscription_IN);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		
		// Periodic activities
		while(true) {
			
			// proxy step
			observingStep();
			
			// look if the resource is in automatic mode
			automatic_mode = controller_IN.isAutomaticMode();
			
if (ProxyClient.debug)
	controller_IN.debugPrintContentStr();

			// if it isn't in automatic mode get value from IN
			if (!automatic_mode)
				sendValue(controller_IN.getValue());
						
			try {
				currentThread();
				Thread.sleep(ProxyClient.T_resource);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/*
	 * Synchronized methods that manages interaction with 6LowPAN phisical resource
	 */
	
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
	
	public boolean isAutomaticMode() {
		return automatic_mode;
	}
	
	
	/*
	 * methods related with CoAP Observing
	 */
	public void cancel(){
		relation.proactiveCancel();
	}
	
	public boolean isCanceled(){
		return relation.isCanceled();
	}
}
