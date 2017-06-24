

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

/*
 * This class create and manage a monitor that waits for oM2M notifications
 */

public class CoAPMonitor extends CoapServer
{
  private int coap_port;
  private String resourceName;
  protected String contentStr;
  protected boolean working;
  
  // Needed to extends CoapServer
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
  
  /*
   * The constructor create a Server with resource 'rn' on local address on port 'port'
   */
  public CoAPMonitor(String rn, int port) throws SocketException
  {
	  coap_port = port;
	  resourceName = rn;
	  contentStr = null;
	  working = false;
	  add(new Resource[] { new Monitor(resourceName) });
  }
  
  public synchronized String getContentStr() {
	  return contentStr;
  }
  
  protected synchronized void setContentStr(String str) {
	  contentStr = str;
  }
  
  /*
   * create a new server CoAP resource
   */
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
    	if (working)
    		setContentStr(exchange.getRequestText());
    	working = true;
    }
  }
}
