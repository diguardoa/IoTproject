import java.io.IOException;


public class ProxyClient {

	public static void main(String[] args) {		
		
		DiVi_ADN br = new DiVi_ADN("http://[aaaa::c30c:0:0:1]");

		// faccio la discovery e faccio partire il sistema
		br.discovery();
		
		// sarebbe utile fare un Thread a livello di ADN che "ogni tot" fa la discovery e aggiorna
		// i pazienti/stanze
		// br.start();
		
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
