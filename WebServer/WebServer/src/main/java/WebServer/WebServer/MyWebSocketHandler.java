package WebServer.WebServer;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

@WebSocket
public class MyWebSocketHandler {

	private Session curr_sess;
	
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
    	curr_sess = session;
    	
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        try {
            session.getRemote().sendString("Hello Webbrowser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
    	try {
    		
    		System.out.println("req: " + message);
			JSONObject request = new JSONObject(message);
			JSONObject response = null;
			int id_type_request = request.getInt("id");
			
			if (id_type_request == 1) 
				response = WebServer.whatPatients();
			else if (id_type_request == 2)
				response = WebServer.whatRooms();
			else 
			{
				String type_entity = request.getString("type");
				String res_name = request.getString("res_name");
				int id_ent = request.getInt("id_ent");
				
				if (type_entity.equals("p"))
				{
					switch (id_type_request) {
					case 3:
						response =  WebServer.getPatient(id_ent).getManager(res_name).getAllValues();
						break;
					case 4:
						response = WebServer.getPatient(id_ent).getManager(res_name).deleteValues();
						break;
					case 5:
						int value_to_post = request.getInt("value");
						response = WebServer.getPatient(id_ent).getManager(res_name).setValue(value_to_post);
						break;
					case 6:
						response = WebServer.getPatient(id_ent).getManager(res_name).setAutomaticMode();
						break;
					case 7:
						response = WebServer.getPatient(id_ent).getManager(res_name).getLastValue();
						break;
					case 8:
						response = WebServer.getPatient(id_ent).getStatus();
						break;
					case 9:
						response = WebServer.getPatient(id_ent).SetAutomaticModeAll();
						break;
					}
				} else {
					// look for a room
					switch (id_type_request) {
					case 3:
						response =  WebServer.getRoom(id_ent).getManager(res_name).getAllValues();
						break;
					case 4:
						response = WebServer.getRoom(id_ent).getManager(res_name).deleteValues();
						break;
					case 5:
						int value_to_post = request.getInt("value");
						response = WebServer.getRoom(id_ent).getManager(res_name).setValue(value_to_post);
						break;
					case 6:
						response = WebServer.getRoom(id_ent).getManager(res_name).setAutomaticMode();
						break;
					case 7:
						response = WebServer.getRoom(id_ent).getManager(res_name).getLastValue();
						break;
					case 8:
						response = WebServer.getRoom(id_ent).getStatus();
						break;
					case 9:
						response = WebServer.getRoom(id_ent).SetAutomaticModeAll();
						break;
					}
				}
			}
			System.out.println("response: " + response.toString());
			curr_sess.getRemote().sendString(response.toString());
		} catch (Exception e) {
			System.out.println("the request is not well formed");
		}
    }
}