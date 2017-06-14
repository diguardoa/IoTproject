package WebServer.WebServer;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

public class CoAPMonitor extends CoapServer
{
  private int coap_port;
  private String resourceName;
  private boolean first_msg;
  private ValueManager manager;
  
  void addEndpoints()
  {
    for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
      if (((addr instanceof Inet4Address)) || (addr.isLoopbackAddress()))
      {
        InetSocketAddress bindToAddress = new InetSocketAddress(addr, coap_port);
        addEndpoint(new CoapEndpoint(bindToAddress));
      }
    }
  }
  
  public CoAPMonitor(String rn, int port, ValueManager manager) throws SocketException
  {
	  this.manager = manager;
	  coap_port = port;
	  resourceName = rn;
	  add(new Resource[] { new Monitor(resourceName) });
	  first_msg = true;
  }
  

class Monitor extends CoapResource
  {
    public Monitor(String rn)
    {
      super(rn);
      
      getAttributes().setTitle(rn);
    }
     
    public void handlePOST(CoapExchange exchange)
    {
    	exchange.respond(ResponseCode.CREATED);
    	byte[] content = exchange.getRequestPayload();
    	if (first_msg)
    		first_msg = false;
    	else {
	        String contentStr = new String(content);
	        //System.out.println(contentStr);
	        try {			
				JSONObject root = new JSONObject(contentStr);
				JSONObject m2msgn = (JSONObject) root.get("m2m:sgn");
				JSONObject nev = (JSONObject) m2msgn.get("nev");
				JSONObject rep = (JSONObject) nev.get("rep");
				
				JSONObject to_save = new JSONObject();
				to_save.put("e", rep.getInt("con"));
				to_save.put("t", rep.get("ct").toString().subSequence(9, 15));
	
				manager.addValue(to_save);
				
				//System.out.println(rep.getInt("con"));
				
if (WebServer.debug)
	manager.printValues();

	        } catch (Exception e) {
				System.out.println("error in decoding msg");
			}
    	}
		
    }
  }
}