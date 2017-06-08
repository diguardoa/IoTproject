import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.californium.core.WebLink;

public class Room extends Thread{
	public static final int treshold_temp = 50;
	
	public int seqNumber;
	
	private int resNumber;
	// Sensors
	private Resource Temp;
	// Actuators (true ones)
	private Resource FireAl;
	private Resource AirCon;


	
	private Container single_room_container;
	private String my_container_long_name;
	
	public Room(int id) {
		seqNumber = id;
		resNumber = 0;
		// Create a patient$seqNumber container into /Patients conteiner
		
		String parent_container = "coap://127.0.0.1:5684/~/DiViProject-mn-cse/DiViProject-mn-name/"
				+ "SmartHospitalization/Rooms";
		String my_container_name = "Room"+String.valueOf(seqNumber);
		
		single_room_container = DiVi_ADN.createContainer(parent_container, my_container_name);		
		
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
				System.out.println("thread started" + String.valueOf(Temp.getValue()));
				if (Temp.getValue() > treshold_temp)
				{
					DiVi_ADN.general_alarm.set();
					// every time a "post" alarm (it is very important)
					System.out.println("Alarm Setted");
					
				}
			}
			
			if (DiVi_ADN.general_alarm.getStatus())
				FireAl.setValue(1);
			
			try {
				currentThread();
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
