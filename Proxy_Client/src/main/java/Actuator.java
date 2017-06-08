import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Actuator extends Resource {
	public Actuator(WebLink link, String parent_container,String uri_server) {
		super(link,parent_container,uri_server);
	}
	public void run() {
		while (true) {
			// only for true actuators
			if (rt.equals("A"))
				observingStep();
			
			try {
				currentThread();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setValue(int value) {
		String message = "e=" + String.valueOf(value);
		CoapClient pclient = new CoapClient(uri_mote);
		CoapResponse post_response = pclient.post(message,MediaTypeRegistry.TEXT_PLAIN);
	}
}
