import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class RoomsAlarm {
	
	private Set<Integer> rooms_on_fire;
	
	public RoomsAlarm() {
		rooms_on_fire = new LinkedHashSet<>();
	}
	
	public synchronized void set(int id_room) {
		rooms_on_fire.add(id_room);
	}
	
	public synchronized boolean getStatus() {
		if (rooms_on_fire.size() == 0)
			return false;
		else
			return true;
	}
	
	public synchronized void reset(int id_room) {
		if (rooms_on_fire.contains(id_room))
				rooms_on_fire.remove(id_room);
	}
}
