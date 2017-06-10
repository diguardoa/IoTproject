package WebServer.WebServer;

import java.util.LinkedList;

import org.eclipse.californium.core.WebLink;

public class WebServer {

	public static LinkedList<Patient> patients;
	public static LinkedList<Room> rooms;
	
	public static int server_coap_port = 5685;
	
	public static void main(String[] args) {
		
		System.out.println("Start Web Server!");
		
		DiVi_ADN_IN adn = new DiVi_ADN_IN();
		LinkedList<String> containers = adn.findContainer();
		String string_ae = containers.getFirst().substring(1);
		containers.removeFirst();
		containers.set(0, containers.getFirst().substring(1));
	
		LinkedList<String> pat_container = adn.findPatientRoom(containers, string_ae +"/Patients");
		if(!pat_container.isEmpty())
			DiVi_ADN_IN.createContainer("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization", 
				"Patients");
		
		patients = new LinkedList<Patient>();
		WebLink link = null, hrs = null, la = null, oxyval = null, te = null, trs = null;
		String parents_ct = null;
		
		for(String s: pat_container){
			if(s.contains(string_ae + "/Patients/Patient0/"))
				link = (new WebLink(s));
			if(s.contains("HRS")) hrs = link;
			if(s.contains("LA")) la = link;
			if(s.contains("OxyValv")) oxyval = link;
			if(s.contains("TE")) te = link;
			if(s.contains("TRS")) trs = link;
		}
		
		parents_ct = "coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization/Patients";
		Patient p1 = new Patient(0, hrs, la, oxyval, te, trs, parents_ct);
		if(!patients.contains(p1))
			patients.add(p1);
				
		LinkedList<String> room_container = adn.findPatientRoom(containers, string_ae +"/Rooms");
		if(!pat_container.isEmpty())
			DiVi_ADN_IN.createContainer("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization", 
				"Rooms");		
		rooms = new LinkedList<Room>();
		link = null;
		te = null; 
		trs = null;
		WebLink aircon = null, fireal = null; 
		
		for(String s: room_container){
			if(s.contains(string_ae + "/Rooms/Room0/"))
				link = (new WebLink(s));
			if(s.contains("AirCon")) aircon = link;
			if(s.contains("FireAl")) fireal = link;
			if(s.contains("TE")) te = link;
			if(s.contains("TRS")) trs = link;
		}
		
		parents_ct = "coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization/Rooms";
		Room r1 = new Room(0, aircon, fireal, te, trs, parents_ct);
		if(!rooms.contains(r1))
			rooms.add(r1);


	}

}
