package Configuration.Configuration;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.UIDefaults.ProxyLazyValue;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;



public class Configuration {
	private String[] room1 = {"5", "8", "9"};
	private String[] pat1 = {"2", "4", "6", "7", "a"};
	private String[] pat2 = {"e", "f", "10", "11", "12"};
	
	
	/*
	 * DiViADN publishes on the MN
	 */
	
	public Configuration() {
		String uri_base_prefix = "coap://[aaaa::c30c:0:0:";
		String uri_base_suffix = "]:5683/id";
		
		// Configure Rooms
		for (int i = 0; i < room1.length; i++) {
			String message = "e=" + String.valueOf(1);
			String address = uri_base_prefix + room1[i] + uri_base_suffix;
			System.out.println(address);
			CoapClient mote_c = new CoapClient(address);
			mote_c.setTimeout(0);
			CoapResponse post_response = mote_c.post(message,MediaTypeRegistry.TEXT_PLAIN);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Configure Patients
		
		// pat1
		for (int i = 0; i < pat1.length; i++) {
			String message = "e=" + String.valueOf(1);
			String address = uri_base_prefix + pat1[i] + uri_base_suffix;
			System.out.println(address);
			CoapClient mote_c = new CoapClient(address);
			mote_c.setTimeout(0);
			CoapResponse post_response = mote_c.post(message,MediaTypeRegistry.TEXT_PLAIN);
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// pat2
		for (int i = 0; i < pat2.length; i++) {
			String message = "e=" + String.valueOf(2);
			String address = uri_base_prefix + pat2[i] + uri_base_suffix;
			System.out.println(address);
			CoapClient mote_c = new CoapClient(address);
			mote_c.setTimeout(0);
			CoapResponse post_response = mote_c.post(message,MediaTypeRegistry.TEXT_PLAIN);
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}