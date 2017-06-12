import java.io.IOException;


public class ProxyClient {
	
	public static int COAP_PORT = 5685;
	
	public static final boolean oM2M_active = true;
	public static final boolean debug = false;
	
	public static final int delay_get_resources = 500;
	public static final int delay_subscription_IN = 10000;
	
	public static final String MN_address = "coap://127.0.0.1:5684/~/DiViProject-mn-cse";
	
	public static final int treshold_HRS_low = 60;
	public static final int treshold_HRS_high = 100;
	public static final int treshold_OxyS_low = 800;
	public static final int treshold_OxyS_high = 1200;
	public static final int treshold_temp_pat_low = 35;
	public static final int treshold_temp_pat_high = 39;
	
	public static final int treshold_temp_room_low = 100;
	public static final int treshold_temp_room_high = 500;
	
	public static final int oxygen_optimal = 1200;
	public static final int temp_room_optimal = 250;
	
	public static final int T_patient = 2000;
	public static final int T_room = 2000;
	public static final int T_resource = 500;
	
	public static final int Kp_oxy = 1;
	public static final double Ki_oxy = 0.1;
	public static final int Kp_temp = 1;
	public static final double Ki_temp = 0.1;
	

	public static void main(String[] args) {		
		
		DiVi_ADN br = new DiVi_ADN("http://[aaaa::212:7401:1:101]");
		
		// discovery lato oM2M finta per vedere come si comporta


		// faccio la discovery e faccio partire il sistema
		br.discovery();
		
		// sarebbe utile fare un Thread a livello di ADN che "ogni tot" fa la discovery e aggiorna
		// i pazienti/stanze
		br.start_pat_rooms();
		
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
