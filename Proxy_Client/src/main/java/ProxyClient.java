import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class ProxyClient {
	public static List<String> motes_add;

	public static void main(String[] args) {
		System.out.println( "Hello World!" );
		
		BorderRouter br = new BorderRouter("http://[aaaa::212:7401:1:101]");
		motes_add = br.getNode();
		Mote m1;
		int i = 0;
		for (String r: motes_add){
			++i;
			//System.out.println(i);
			 m1 = new Mote(r);
		}
			//System.out.println(r);
		
		

		/*
		 * Creiamo sul MN tanti AE quanti i nodi
		 */
		
		/*
		 * Discovery sui nodi
		 */
		
		/*
		 * Creiamo tanti Conteiner quante le risorse
		 */
		
		/*
		 * Creiamo tanti client quante le risorse
		 */
		/*
		CoapClient client = new CoapClient(uri);
		System.out.println( "Client Created" );
		
        	System.out.println( "pre get" );
        	CoapResponse response	=	client.get();
        	//System.out.println(client.discover());
        	
        	System.out.println( "after get" );
        	System.out.println(new String(response.getPayload()));
        	*/
		while(true) {
        	try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        }

	}
	
}
