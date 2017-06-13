
import org.eclipse.californium.core.WebLink;

public class Patient extends Thread {
	public int seqNumber;
	private int resNumber;
		
	// Sensors
	private Resource Temp;
	private Resource HRS;
	private Resource OxyS;
	// Actuators (true ones)
	private Resource OxyValve;
	private Resource LedA;
	
	// Control Variables
	private int e_Oxy;
	private int e_Oxy_int;
	
	private String my_container_long_name;
	
	public Patient(int id) {
		seqNumber = id;
		resNumber = 0;
		e_Oxy = 0;
		e_Oxy_int = 0;
		
		// Create a patient$seqNumber container into /Patients conteiner
		
		String parent_container = ProxyClient.MN_address + "/DiViProject-mn-name/"
				+ "SmartHospitalization/Patients";
		String my_container_name = "Patient"+String.valueOf(seqNumber);
		
		DiVi_ADN.createContainer(parent_container, my_container_name);		
		
		my_container_long_name = parent_container + "/" + my_container_name;
				
		
	}
	
	public void addResource(WebLink link, String res_uri) {
		System.out.println(link.getURI());
		
		String res_title = link.getAttributes().getTitle();
				
		switch (res_title) {
		case "Temp":
			Temp = new Resource(link,my_container_long_name,res_uri);
			Temp.start();
			resNumber++;
			break;
		case "HRS":
			HRS = new Resource(link,my_container_long_name,res_uri);
			HRS.start();
			resNumber++;
			break;
		case "OxyS":
			OxyS = new Resource(link,my_container_long_name,res_uri);
			OxyS.start();
			resNumber++;
			break;
		case "OxyValv":
			OxyValve = new Resource(link,my_container_long_name,res_uri);
			OxyValve.start();
			resNumber++;
			break;
		case "LedA":
			LedA = new Resource(link,my_container_long_name,res_uri);
			LedA.start();
			resNumber++;
			break;
		default:
			System.out.println(res_title + " was not recognized");	
		}

			
	}
	
	public void run() {
		while (true) {
			if (resNumber == 5) {
				

				// get temp variables
				int t_HRS = HRS.getValue();
				int t_OxyS = OxyS.getValue();
				int t_temp = Temp.getValue();
if (ProxyClient.debug)
	System.out.println("thread patient " + seqNumber);
				// look for errors
				if ((t_HRS < ProxyClient.treshold_HRS_low) || (t_HRS > ProxyClient.treshold_HRS_low) ||
						(t_OxyS < ProxyClient.treshold_OxyS_low) || (t_OxyS > ProxyClient.treshold_OxyS_high) ||
						(t_temp < ProxyClient.treshold_temp_pat_low) || (t_temp > ProxyClient.treshold_temp_pat_high))
				{
					LedA.setValue(1);
				}
				
				// Adjust Oxigen (PI)
				e_Oxy = ProxyClient.oxygen_optimal - t_OxyS;
				e_Oxy_int += e_Oxy;
				int u = ProxyClient.Kp_oxy * e_Oxy + (int) (ProxyClient.Ki_oxy*e_Oxy_int);
				OxyS.setValue(u);
				OxyValve.setValue(u);
				
			}
			
			try {
				currentThread();
				Thread.sleep(ProxyClient.T_patient);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
