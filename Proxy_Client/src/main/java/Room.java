import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.californium.core.WebLink;

public class Room extends Thread{
	public int seqNumber;
	
	private int e_Temp;
	private int e_Temp_int;
	
	private int resNumber;
	// Sensors
	private Resource TempR;
	// Actuators (true ones)
	private Resource FireAl;
	private Resource AirCon;
	
	private String my_container_long_name;
	
	public Room(int id) {
		seqNumber = id;
		resNumber = 0;
		// Create a patient$seqNumber container into /Patients conteiner
		
		String parent_container = ProxyClient.MN_address + "/DiViProject-mn-name/"
				+ "SmartHospitalization/Rooms";
		String my_container_name = "Room"+String.valueOf(seqNumber);
		
		DiVi_ADN.createContainer(parent_container, my_container_name);		
		
		my_container_long_name = parent_container + "/" + my_container_name;
				
	}
	
	public void addResource(WebLink link, String res_uri) {
if (ProxyClient.debug)	
	System.out.println(link.getURI());
		
		String res_title = link.getAttributes().getTitle();
				
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

	public void run() {
		while (true) {
			
			if (resNumber == 3) {
				int t_temp = TempR.getValue();
if (ProxyClient.debug)	
	System.out.println("thread started " + seqNumber);
				
				// Adjust Temperature (PI)
				e_Temp = ProxyClient.temp_room_optimal - t_temp;
				e_Temp_int += e_Temp;
				int u = ProxyClient.Kp_temp * e_Temp + (int) (ProxyClient.Ki_temp*e_Temp_int);
				TempR.setValue(u);
				AirCon.setValue(u);				
				
				
				if ((t_temp > ProxyClient.treshold_temp_room_high) || 
						(t_temp < ProxyClient.treshold_temp_room_low))
				{
					DiVi_ADN.general_alarm.set();
				}
			}
			
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
