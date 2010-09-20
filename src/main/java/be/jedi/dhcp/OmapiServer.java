package be.jedi.dhcp;

import org.talamonso.OMAPI.Connection;
import org.talamonso.OMAPI.Message;
import org.talamonso.OMAPI.Exceptions.OmapiConnectionException;
import org.talamonso.OMAPI.Exceptions.OmapiException;
import org.talamonso.OMAPI.Exceptions.OmapiInitException;
import org.talamonso.OMAPI.Exceptions.OmapiObjectException;
import org.talamonso.OMAPI.Objects.Host;

public class OmapiServer {

	public OmapiServer(String omapiHost, int omapiPort, String omapiKeyName,
			String omapiKeyValue) {
		super();
		this.omapiHost = omapiHost;
		this.omapiPort = omapiPort;
		this.omapiKeyName = omapiKeyName;
		this.omapiKeyValue = omapiKeyValue;
	}

	String omapiHost; 
	int omapiPort;
	String omapiKeyName;
	String omapiKeyValue;
	
	 void updateDHCP(String name, String macAddress) throws OmapiInitException, OmapiConnectionException {
			Connection c = null;
			try {
				c = new Connection(omapiHost, omapiPort);
				c.setAuth(omapiKeyName, omapiKeyValue);
			} catch (OmapiException e) {
				System.err.println(e.getMessage());
			}

			try {

				Host h3 = new Host(c);
				h3.setHardwareAddress(macAddress);
				// h.setName("albert");
				Host remote3 = h3.send(Message.OPEN);
				
				Host h = new Host(c);
				h.updateIPAddress("1.3.1.46");
				h.setName("albert2");
				Host remote = h.send(Message.UPDATE);
				
				Host h2 = new Host(c);
				h2.setName("albert");
				h2.setIPAddress("1.2.3.4");
				h2.setHardwareAddress(macAddress);
				h2.setHardwareType(1);
				Host remote2 = h2.send(Message.CREATE);
				
				
			} catch (OmapiObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	  
	  }
}
