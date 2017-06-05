import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.californium.core.WebLink;

public class Patient extends Thread {
	public int seqNumber;
	public int resNumber;
	private List<Resource> resources = new LinkedList<>();;
	private Container single_patient_container;
	private String my_container_long_name;
	
	public Patient(int id) {
		seqNumber = id;
		resNumber = 0;

		
		// Create a patient$seqNumber container into /Patients conteiner
		
		String parent_container = "coap://127.0.0.1:5684/~/DiViProject-mn-cse/DiViProject-mn-name/"
				+ "SmartHospitalization/Patients";
		String my_container_name = "Patient"+String.valueOf(seqNumber);
		
		single_patient_container = DiVi_ADN.createContainer(parent_container, my_container_name);		
		
		my_container_long_name = parent_container + "/" + my_container_name;
				
	}
	
	public void addResource(WebLink link, String res_uri) {
		System.out.println(link.getURI());
		
		String res_title = link.getAttributes().getTitle();
		
		// Look for the resource, if it exist. If it is not create it
		List<Resource> look_for_resource = resources.stream()
				.filter(a -> Objects.equals(a.resource_name, res_title))
				.collect(Collectors.toList());	
		
		if (look_for_resource.isEmpty())
			resources.add(new Resource(link,my_container_long_name,res_uri));
			
	}
	
	public void run() {
		/*
		 *  codice che 
		 *  1) guarda se ha ancora risorse, se non ne ha più elimina il paziente
		 *  2) prende tutte le decisioni relative al singolo paziente (controller, setta i valori degli attuatori)
		 *  
		 */
	}
}
