
public class RoomsAlarm {
	private boolean status;
	
	public RoomsAlarm() {
		status = false;
	}
	
	public synchronized void set() {
		status = true;
	}
	
	public synchronized boolean getStatus() {
		return status;
	}
	
	public synchronized void reset() {
		status = false;
	}
}
