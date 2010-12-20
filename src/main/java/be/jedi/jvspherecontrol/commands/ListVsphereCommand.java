package be.jedi.jvspherecontrol.commands;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;

import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;
import be.jedi.jvspherecontrol.vsphere.VsphereServer;

public class ListVsphereCommand extends VsphereCommand {


	public static String keyword="list"; 
	public static String description="this list various things of the vsphere server"; 

	String[] listItems= { "hosts", "datacenters", "datastores" , "networks" , "users", "vms", "all"};
	String listItem="all";
	
	public String getKeyword() {
		return keyword;
	}

	public String getDescription() {
		return description;
	}

	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException{

		super.validateArgs();

		//We check for the list option to be part of the list we know
		String firstArg=this.getArgs()[0];
		
		if (!Arrays.asList(listItems).contains(firstArg)) {
			throw new InvalidCLIArgumentSyntaxException("the options specified to the list command is not valid");
		} else {
			listItem=firstArg;
		}
		
	}

	public void execute() {

		VsphereServer vsphereServer=new VsphereServer(vsphereUrl, vsphereUsername,vspherePassword);
		try {

			vsphereServer.connect();

			if ((listItem.equals("hosts")) || (listItem.equals("all"))) {
				for (String host : vsphereServer.listHosts()) {
					System.out.println("Host found: "+host);
				}
			}

			if ((listItem.equals("datacenters")) || (listItem.equals("all"))) {
				for (String datacenter :vsphereServer.listDataCenters() ) {
					System.out.println("Datacenter found: "+datacenter);					
				}	
			}
			
			if ((listItem.equals("datastores")) || (listItem.equals("all"))) {
				for (String datastore :vsphereServer.listDataStores() ) {
					System.out.println("Datastore found: "+datastore);					
				}
			}

			if ((listItem.equals("networks")) || (listItem.equals("all"))) {

				for (String network :vsphereServer.listNetworks() ) {
					System.out.println("Network found: "+network);					
				}
			}
			
			if ((listItem.equals("users")) || (listItem.equals("all"))) {

				for (String user :vsphereServer.listUsers() ) {
					System.out.println("User found: "+user);					
				}	
			}

			if ((listItem.equals("vms")) || (listItem.equals("all"))) {
			for (String vm :vsphereServer.listVms() ) {
				System.out.println("Vm found: "+vm);					
			}
			}

		} catch (RemoteException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getHelp() {

		String listItemsText="";
		for (int i=0; i< listItems.length; i++) {
			listItemsText+=listItems[i];
			if (i < listItems.length -1) {
				listItemsText+="|";
			}
		}
		return listItemsText+"\n"+super.getHelp();
	}
}