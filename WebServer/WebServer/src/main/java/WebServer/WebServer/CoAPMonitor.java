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

public class CoAPMonitor extends CoapServer
{
  private int coap_port;
  private String resourceName;
  
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
  
  public CoAPMonitor(String rn, int port) throws SocketException
  {
	  coap_port = port;
	  resourceName = rn;
	  add(new Resource[] { new Monitor(resourceName) });
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
        String contentStr = new String(content);
        System.out.println(contentStr);
    }
  }
}