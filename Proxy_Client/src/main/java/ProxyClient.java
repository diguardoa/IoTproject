import java.io.IOException;


public class ProxyClient {

	public static void main(String[] args) {
		System.out.println( "Hello World!" );
		
		BorderRouter br = new BorderRouter("http://[aaaa::c30c:0:0:1]");

		br.discovery();
		

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
