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
;
		searchHost.setName(name);
		try {
			Host existingHost=searchHost.send(Message.OPEN);
			existingHost.delete();

		} catch (OmapiObjectException e) {
			// Object does not exist
			// So we can try to create it				
		}

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
}
