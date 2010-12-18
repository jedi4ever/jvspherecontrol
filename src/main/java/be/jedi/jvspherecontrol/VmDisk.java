package be.jedi.jvspherecontrol;

public class VmDisk {
	
	long size=10000;
	String mode="persistent";
	String datastore=null;
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getDatastore() {
		return datastore;
	}
	public void setDatastore(String datastore) {
		this.datastore = datastore;
	}

}
