package WebServer.WebServer;

import java.util.LinkedList;

public class WebServer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("ciao!");
		
		DiVi_ADN_IN adn = new DiVi_ADN_IN();
		LinkedList<String> containers = adn.prova();
		String string_ae = containers.getFirst();
		containers.removeFirst();
		//System.out.println(containers.getFirst());
		//containers.addcontainers.getFirst().substring(1);
		System.out.println(containers.getFirst());
		adn.findPatientRoom(containers, string_ae +"/Patients");
		//string_ae = string_ae.substring(1);
		//adn.findPatientRoom(containers, string_ae +"/Rooms");
	}

}
