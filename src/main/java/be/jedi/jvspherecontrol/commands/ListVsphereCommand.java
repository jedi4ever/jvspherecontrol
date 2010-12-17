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
				vsphereServer.listDataCenters();
				vsphereServer.listDataStores();
				vsphereServer.listHosts();
				vsphereServer.listNetworks();

			} catch (RemoteException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}