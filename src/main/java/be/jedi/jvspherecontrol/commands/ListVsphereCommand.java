package be.jedi.jvspherecontrol.commands;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import be.jedi.jvspherecontrol.vsphere.VsphereServer;

public class ListVsphereCommand extends VsphereCommand {


	public static String keyword="list"; 
	public static String description="this creates a virtual machine"; 

	 public String getKeyword() {
		return keyword;
	}

	 public String getDescription() {
		return description;
	}

	public void execute() {
		
		VsphereServer vsphereServer=new VsphereServer(vsphereUrl, vsphereUsername,vspherePassword);
			try {
				
				vsphereServer.connect();
				
				for (String host : vsphereServer.listHosts()) {
					System.out.println("Host found: "+host);
				}
				
				for (String datacenter :vsphereServer.listDataCenters() ) {
					System.out.println("Datacenter found: "+datacenter);					
				}

				for (String datastore :vsphereServer.listDataStores() ) {
					System.out.println("Datastore found: "+datastore);					
				}

				for (String network :vsphereServer.listNetworks() ) {
					System.out.println("Network found: "+network);					
				}

				for (String user :vsphereServer.listUsers() ) {
					System.out.println("User found: "+user);					
				}

				for (String vm :vsphereServer.listVms() ) {
					System.out.println("Vm found: "+vm);					
				}

				
			} catch (RemoteException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}