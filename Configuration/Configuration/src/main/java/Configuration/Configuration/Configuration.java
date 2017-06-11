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
	private String[] room1 = {"0c:c:c0c", "0f:f:f0f", "10:10:1010"};
	private String[] room2 = {"0e:e:e0e", "0d:d:d0d", "11:11:1111"};
	private String[] pat1 = {"03:3:303", "05:5:505", "07:7:707", "09:9:909", "0a:a:a0a"};	
	private String[] pat2 = {"02:2:202", "04:4:404", "06:6:606", "08:8:808", "0b:b:b0b"};	
	private String uri_base_prefix = "coap://[aaaa::212:74";
	private String uri_base_suffix = "]:5683/id";
	
	public void simulation7() {

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
		
		for (int i = 0; i < room2.length; i++) {
			String message = "e=" + String.valueOf(2);
			String address = uri_base_prefix + room2[i] + uri_base_suffix;
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