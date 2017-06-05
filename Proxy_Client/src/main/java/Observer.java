

import java.net.URI;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.json.JSONObject;


public class Observer extends Thread{
	private final String uri;
	private CoapClient observable;
	private CoapObserveRelation relation;
	public String parent_container;
	public CoapResponse to_publish;
	
	public void run() {
		while (true) {
			if (to_publish!=null) {
				// All msg are conformed with SenML standard
				JSONObject jsonOBJ = new JSONObject(to_publish.getResponseText());
				String value = jsonOBJ.get("e").toString();
				String mes_unity = jsonOBJ.get("u").toString();
				DiVi_ADN.createContentInstance(parent_container, mes_unity, value);
				to_publish = null;
			}
			try {
				currentThread();
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Observer(String uri_mote, String resource_mn_path){
		this.parent_container = resource_mn_path;
		this.uri = uri_mote;
		System.out.println(uri_mote);
		
		observable = new CoapClient(this.uri);
		relation = observable.observe(new CoapHandler() {
			@Override
			public void onLoad(final CoapResponse curr_response) 
			{
				if (curr_response != null) {
					to_publish = curr_response;
				} else
					System.out.println("null response");
			}
			
			@Override
			public void onError() {
				System.out.println("Error!");				
			}
			
		});
	}
	
	public void cancel(){
		relation.proactiveCancel();
	}
	
	public boolean isCanceled(){
		return relation.isCanceled();
	}
}
