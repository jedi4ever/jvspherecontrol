package be.jedi.jvspherecontrol.dhcp;

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
	
	public void updateDHCP(String name, String macAddress, boolean overwrite) throws OmapiInitException, OmapiConnectionException {
			Connection c = null;
			try {
				c = new Connection(omapiHost, omapiPort);
				c.setAuth(omapiKeyName, omapiKeyValue);
			} catch (OmapiException e) {
				System.err.println(e.getMessage());
			}

				Host searchHost = new Host(c);

//				host.setHardwareAddress(macAddress);
				searchHost.setName(name);
				try {
					Host existingHost=searchHost.send(Message.OPEN);
					existingHost.setHardwareAddress(macAddress);
					existingHost.setHardwareType(1);
					existingHost.send(Message.UPDATE);					
					//maybe mac address search too?
					
				} catch (OmapiObjectException e) {
					// Object does not exist
					// So we can try to create it				

					try {
						Host newHost = new Host(c);
						newHost.setName(name);
						newHost.setHardwareAddress(macAddress);
						newHost.setHardwareType(1);
						newHost.send(Message.CREATE);
					} catch (OmapiObjectException e1) {
						// Update failed for some reason
						e1.printStackTrace();
					}
					
				}
				
				
//				Host h3 = new Host(c);
//				h3.setHardwareAddress(macAddress);
//				// h.setName("albert");
//				Host remote3 = h3.send(Message.OPEN);
//				
//				Host h = new Host(c);
//				h.updateIPAddress("1.3.1.46");
//				h.setName("albert2");
//				Host remote = h.send(Message.UPDATE);
//				
//				Host h2 = new Host(c);
//				h2.setName("albert");
//				h2.setIPAddress("1.2.3.4");
//				h2.setHardwareAddress(macAddress);
//				h2.setHardwareType(1);
//				Host remote2 = h2.send(Message.CREATE);


	  
	  }
}
