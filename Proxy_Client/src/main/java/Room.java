
import org.eclipse.californium.core.WebLink;
/*
 * Room Class manages resources that belongs to a particular room instance
 */
public class Room extends Thread{
	public int seqNumber;
	
	private double e_Temp;
	private double e_Temp_int;
	private int last_value;
	
	private int resNumber;
	// Sensors
	private Resource TempR;
	// Actuators 
	private Resource FireAl;
	private Resource AirCon;
	
	private String my_container_long_name;
	
	/*
	 * The constructor create a room instance with the dived ID. They cannot
	 * exist two different rooms with the same ID
	 */
	
	public Room(int id) {
		seqNumber = id;
		resNumber = 0;
		last_value = 0;
		// Create a patient$seqNumber container into /Patients conteiner
		
		String parent_container = ProxyClient.MN_address + "/DiViProject-mn-name/"
				+ "SmartHospitalization/Rooms";
		String my_container_name = "Room"+String.valueOf(seqNumber);
		
		DiVi_ADN.createContainer(parent_container, my_container_name);		
		
		my_container_long_name = parent_container + "/" + my_container_name;
				
	}
	
	/*
	 * This function add a resource to the room
	 */
	public void addResource(WebLink link, String res_uri) {
if (ProxyClient.debug)	
	System.out.println(link.getURI());
		
		String res_title = link.getAttributes().getTitle();
		
		// resource is classified depending on its title attribute. 
		switch (res_title) {
		case "TempR":
			TempR = new Resource(link,my_container_long_name,res_uri);
			TempR.start();
			resNumber++;
			break;
		case "AirCon":
			AirCon = new Resource(link,my_container_long_name,res_uri);
			AirCon.start();
			resNumber++;
			break;
		case "FireAl":
			FireAl = new Resource(link,my_container_long_name,res_uri);
			FireAl.start();
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
			
			if (resNumber == 3) {
				int t_temp = TempR.getValue();
				
if (ProxyClient.debug)	
	System.out.println("thread started " + seqNumber);

				
				if (TempR.isAutomaticMode())
				{
							// Adjust Temperature (PI with saturation and some rubbish to limit awful behaviors)
							e_Temp = ProxyClient.temp_room_optimal - t_temp;
							e_Temp_int += e_Temp;
							int u = (int) (ProxyClient.Kp_temp * e_Temp) + (int) (ProxyClient.Ki_temp*e_Temp_int)+last_value;
							
							// PI saturation
							if (u < ProxyClient.temp_room_min)
								u = ProxyClient.temp_room_min;
							if (u > ProxyClient.temp_room_max)
								u = ProxyClient.temp_room_max;
							
							TempR.setValue(u);
							AirCon.setValue(u);				
							
							
							
				} else
				{
					e_Temp_int = 0;
					last_value = t_temp;
				}
				// Manage alarm
				if ((t_temp > ProxyClient.treshold_temp_room_high) || 
						(t_temp < ProxyClient.treshold_temp_room_low))
				{
					DiVi_ADN.general_alarm.set(seqNumber);
				} else {
					DiVi_ADN.general_alarm.reset(seqNumber);
				}
			}
			
			// Set the fire alarm whether any room had been temperature problem
			if (DiVi_ADN.general_alarm.getStatus())
				FireAl.setValue(1);
			
			try {
				currentThread();
				Thread.sleep(ProxyClient.T_room);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
