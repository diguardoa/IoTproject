import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class BorderRouter {
	public URI uri;
	public List<String> addresses = new LinkedList<>();
	
	public BorderRouter(String string) {
		uri = null;
		try {
			uri = new URI(string);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}		
	}

	public List<String> getNode() {
		
		HttpClient client = new DefaultHttpClient();
				
		HttpUriRequest request = new HttpGet(uri);
		System.out.println(uri);
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String temp = EntityUtils.toString(response.getEntity());
			String[] parts = temp.split("</pre>Routes<pre>");
			parts = parts[1].split("</pre></body></html>");
			
			// divido per righe
			String[] rows = parts[0].split(Pattern.quote("\n"));
			for (String row: rows) 
			{
				String[] first_part_row = row.split("/128");
				addresses.add("coap://[" + first_part_row[0] + "]:5683");//.well-known/core/");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addresses;
		
	}
}
