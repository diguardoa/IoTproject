import java.io.IOException;


public class ProxyClient {

	public static void main(String[] args) {
		System.out.println( "Hello World!" );
		
		DiVi_ADN br = new DiVi_ADN("http://[aaaa::c30c:0:0:1]");

		br.discovery();
		
		System.out.println("end");
		
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
