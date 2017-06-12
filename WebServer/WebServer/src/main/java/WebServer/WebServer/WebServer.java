package WebServer.WebServer;

import java.util.LinkedList;

import org.eclipse.californium.core.WebLink;

public class WebServer {

	public static LinkedList<Patient> patients;
	public static LinkedList<Room> rooms;
	
	public static int server_coap_port = 5800;
	
	private static LinkedList<Room> createRoom(LinkedList<String> room_container){
		LinkedList<Room> ro = new LinkedList<Room>();
		
		String parents_ct = "coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization/Rooms";
		WebLink aircon = null, fireal = null, tempr = null, link = null; 
		int id = -1;
		int count_room = 0;
		int count = ("DiViProject-mn-cse/DiViProject-mn-name/SmartHospitalization/rooms/RoomX").length();
		
		//Count how many rooms
		for(String r: room_container)
			if(r.length() == count)
				count_room++;

		//Search rooms' name
		String[] rooms = new String[count_room];
		int i = 1;
		for(String s: room_container){
			if(s.contains("/Room"+i) && (!s.contains("/Room"+i+"/"))){
				rooms[i-1] = new String("Room"+i);
				i++;
			}
		}
		
		//For each patient we look for the proper resources
		for(String r: rooms){
			id = new Integer(r.substring(4,5));
			for(String s: room_container){
				if(s.contains("/Rooms/Room" + id +"/"))
					link = (new WebLink(s));
				if(s.endsWith("/Room" + id + "/AirCon")) aircon = link;
				if(s.endsWith("/Room" + id + "/FireAl")) fireal = link;
				if(s.endsWith("/Room" + id + "/TempR")) tempr = link;
			}
		
			Room r1 = new Room(id, aircon, fireal, tempr, parents_ct);
			if(!ro.contains(r1))
				ro.add(r1);
		}
		
		return ro;
	}
	
	private static LinkedList<Patient> createPatient(LinkedList<String> pat_container){
		LinkedList<Patient> pt_list = new LinkedList<Patient>();
		
		WebLink link = null, hrs = null, la = null, oxyval = null, temp = null, oxys = null;
		String parents_ct = "coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization/Patients";
		int id = -1;
		int count_pat = 0;
		int count = ("DiViProject-mn-cse/DiViProject-mn-name/SmartHospitalization/Patients/PatientX").length();

		//Count how many patients
		for(String s: pat_container)
			if(s.length() == count)
				count_pat++;
		
		//Search patients' name
		String[] pats = new String[count_pat];
		int i = 1;
		for(String s: pat_container){
			if(s.contains("/Patient"+i) && (!s.contains("/Patient"+i+"/"))){
				pats[i-1] = new String("Patient"+ i);
				i++;
			}
		}
		
		//For each patient we look for the proper resources
		for(String pa: pats){
			id = new Integer(pa.substring(7,8));
			for(String s: pat_container){

				if(s.contains("/Patients/Patient" + id + "/") ){
					link = (new WebLink(s));
				}
				
				if(s.endsWith("/Patient"+ id + "/HRS")) hrs = link;
				if(s.endsWith("/Patient"+ id + "/LedA")) la = link;
				if(s.endsWith("/Patient"+ id + "/OxyValv")) oxyval = link;
				if(s.endsWith("/Patient"+ id + "/Temp")) temp = link;
				if(s.endsWith("/Patient"+ id + "/OxyS")) oxys = link;
			}
			
			Patient p = new Patient(id, hrs, la, oxyval, temp, oxys, parents_ct);
			if(!pt_list.contains(p))
				pt_list.add(p);
		}
		
		return pt_list;
	}
	
	public static void main(String[] args) {
		
		System.out.println("Start Web Server!");
		
		DiVi_ADN_IN adn = new DiVi_ADN_IN();
		LinkedList<String> containers = adn.findContainer();
		String string_ae = containers.getFirst().substring(1);
		containers.removeFirst();
		containers.set(0, containers.getFirst().substring(1));
		LinkedList<String> pat_container = adn.findPatientRoom(containers, string_ae +"/Patients");
		
		//for(String s: pat_container)
			//System.out.println(s);
		
		if(!pat_container.isEmpty())
			DiVi_ADN_IN.createContainer("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization", 
				"Patients");
		
		patients = createPatient(pat_container);
		
		LinkedList<String> room_container = adn.findPatientRoom(containers, string_ae +"/Rooms");
		if(!room_container.isEmpty())
			DiVi_ADN_IN.createContainer("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization", 
				"Rooms");		
		
		rooms = createRoom(room_container);

	}

}
