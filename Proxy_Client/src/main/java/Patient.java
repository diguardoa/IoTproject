
import org.eclipse.californium.core.WebLink;

/*
 * Patient Class manages resources that belongs to a particular patient instance
 */
public class Patient extends Thread {
	public int seqNumber;
	private int resNumber;
		
	// Sensors
	private Resource Temp;
	private Resource HRS;
	private Resource OxyS;
	// Actuators 
	private Resource OxyValve;
	private Resource LedA;
	
	// Control Variables
	private double e_Oxy;
	private double e_Oxy_int;
	private int last_value;
	
	private String my_container_long_name;
	
	/*
	 * The constructor create a patient instance with the dived ID. They cannot
	 * exist two different patients with the same ID
	 */
	
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
	
	/*
	 * This function add a resource to the patient
	 */
	public void addResource(WebLink link, String res_uri) {
		System.out.println(link.getURI());
		
		String res_title = link.getAttributes().getTitle();
				
		// resource is classified depending on its title attribute. 
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
	
/*
 * This pseudo periodic function perform the control of
 * resources according with requisites
 */
	public void run() {
		while (true) {
			
			// Control process works iff all resources are identified
			if (resNumber == 5) {

				// get status variables
				int t_HRS = HRS.getValue();
				int t_OxyS = OxyS.getValue();
				int t_temp = Temp.getValue();
				
if (ProxyClient.debug)
	System.out.println("thread patient " + seqNumber);

				/*
				 * The alarm starts iff some vital parameter is outside 
				 * given thresholds
				 */
				if ((t_HRS < ProxyClient.treshold_HRS_low) || (t_HRS > ProxyClient.treshold_HRS_high) ||
						(t_OxyS < ProxyClient.treshold_OxyS_low) || (t_OxyS > ProxyClient.treshold_OxyS_high) ||
						(t_temp < ProxyClient.treshold_temp_pat_low) || (t_temp > ProxyClient.treshold_temp_pat_high))
				{
					LedA.setValue(1);
				}
				
				// THe control works Only in Automatic mode.
				if (OxyS.isAutomaticMode())
				{
								
							// Adjust Oxigen (PI with saturation and some rubbish to limit awful behaviors)
							e_Oxy = ProxyClient.oxygen_optimal - t_OxyS;
							e_Oxy_int += e_Oxy;
							int u = (int) (ProxyClient.Kp_oxy * e_Oxy) + (int) (ProxyClient.Ki_oxy*e_Oxy_int)+last_value;
							
							// PI saturation
							if (u < ProxyClient.oxy_min)
							{
								u = ProxyClient.oxy_min;
							}
							if (u > ProxyClient.oxy_max)
							{
								u = ProxyClient.oxy_max;
							}
							
							OxyS.setValue(u);
							OxyValve.setValue(u);
							
				} else 
				{
					// clean I
					e_Oxy_int = 0;
					// manage rubbish for PI management
					last_value = t_OxyS;
				}
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
