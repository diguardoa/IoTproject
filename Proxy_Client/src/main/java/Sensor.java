import org.eclipse.californium.core.WebLink;

public class Sensor extends Resource {
	public Sensor(WebLink link, String parent_container,String uri_server) {
		super(link,parent_container,uri_server);
	}
	
	public void run() {
		while (true) {
			
			observingStep();
			try {
				currentThread();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
