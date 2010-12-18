package be.jedi.jvspherecontrol;

public class VmNic {
	
	String name=null;
	String type="e1000";
	String network=null;
	boolean connected=true;
	boolean startConnected=true;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	public boolean isStartConnected() {
		return startConnected;
	}
	public void setStartConnected(boolean startConnected) {
		this.startConnected = startConnected;
	}

	
}
