package WebServer.WebServer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.californium.core.WebLink;

public class WebServer {
	

	public static final int WebSocketPort = 8100;
	public static final boolean debug = false;
	
	public static Server webAppServer;

	public static LinkedList<Patient> patients;
	public static LinkedList<Room> rooms;
	
	//SERVER COAP PORT - INITIAL VALUE
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
			
			//Creation of new Patient, if the patient is not in the Patient list is added
			Patient p = new Patient(id, hrs, la, oxyval, temp, oxys, parents_ct);
			if(!pt_list.contains(p))
				pt_list.add(p);
		}
		
		return pt_list;
	}
	
	//Manager for incoming message with id = 1
	static JSONObject whatPatients() {
		JSONObject resp = new JSONObject();
		JSONArray payload_array = new JSONArray();
		
		for(Patient p: patients)
		{
			JSONObject p_entity = new JSONObject();
			p_entity.put("e", p.id);
			payload_array.put(p_entity);
		}
		
		resp.put("id", 1);
		resp.put("desc", "WhatPatients");
		resp.put("payload", payload_array);
		
		return resp;
	}
	
	//Manager for incoming with id = 2
	static JSONObject whatRooms() {
		JSONObject resp = new JSONObject();
		JSONArray payload_array = new JSONArray();
		
		for(Room r: rooms)
		{
			JSONObject p_entity = new JSONObject();
			p_entity.put("e", r.id);
			payload_array.put(p_entity);
		}
		
		resp.put("id", 2);
		resp.put("desc", "WhatRooms");
		resp.put("payload", payload_array);
		
		return resp;
	}
	
	//Get the patient with id equals the "id" within the incoming message
	static Patient getPatient(int id) {
		List<Patient> look_for_patient = patients.stream()
				.filter(a -> Objects.equals(a.id, id))
				.collect(Collectors.toList());
		
		return look_for_patient.get(0);
	}
	
	//Get the room with id equals the "id" within the incoming message
	static Room getRoom(int id) {
		List<Room> look_for_room = rooms.stream()
				.filter(a -> Objects.equals(a.id, id))
				.collect(Collectors.toList());
		
		return look_for_room.get(0);
	}
	
	//Create an ADN with AE SmartHospitalization
	//Populate the AE with the value obtained by the get on the MN
	//Open a webSocketPort to talk with the WebApp
	public static void main(String[] args) throws Exception {
		
		System.out.println("Start Web Server!");
		
		DiVi_ADN_IN adn = new DiVi_ADN_IN();
		//Search for containers and parse the string obtained as response
		LinkedList<String> containers = adn.findContainer();
		String string_ae = containers.getFirst().substring(1);
		containers.removeFirst();
		containers.set(0, containers.getFirst().substring(1));
		LinkedList<String> pat_container = adn.findPatientRoom(containers, string_ae +"/Patients");
		
		if(!pat_container.isEmpty())
			DiVi_ADN_IN.createContainer("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization", 
				"Patients");
		
		patients = createPatient(pat_container);
		
		LinkedList<String> room_container = adn.findPatientRoom(containers, string_ae +"/Rooms");
		if(!room_container.isEmpty())
			DiVi_ADN_IN.createContainer("coap://127.0.0.1:5683/~/DiViProject-in-cse/DiViProject-in-name/SmartHospitalization", 
				"Rooms");		
		
		rooms = createRoom(room_container);
		
		webAppServer = new Server(WebSocketPort);
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(MyWebSocketHandler.class);
            }
        };
        webAppServer.setHandler(wsHandler);
        webAppServer.start();
        webAppServer.join();
	}
}
