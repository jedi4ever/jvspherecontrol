package be.jedi.jvspherecontrol.commands;



import org.apache.commons.cli.OptionBuilder;


import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;
import be.jedi.jvspherecontrol.vsphere.VsphereServer;

import com.vmware.vim25.mo.VirtualMachine;

public class ActivateVncInVmCommand extends VsphereCommand  {

	public static String keyword="activateVncInVm"; 
	public static String description="this creates a virtual machine";
	
	public String vmName;
	
	public String vncPassword;
	public int vncPort;
		
	public ActivateVncInVmCommand() {
		super();
	}

	public void init(String args[]) {
		super.init(args);
	}

	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException{
		
		super.validateArgs();
		
		vmName=cmdLine.getOptionValue("vmName");
		vncPort=Integer.parseInt(cmdLine.getOptionValue("vncPort"));
		vncPassword=cmdLine.getOptionValue("vncPassword");
	}
	
	void initOptions() {
		super.initOptions();

		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of vm to create" ).create( "vmName" ));
		options.addOption(OptionBuilder.withArgName( "portnumber" ).hasArg().withDescription(  "port to enable vnc on" ).create( "vncPort" ));
		options.addOption(OptionBuilder.withArgName( "pqssword" ).hasArg().withDescription(  "name of vm to create" ).create( "vncPassword" ));
	}
	
	
	public void execute(){
		
		VsphereServer vsphereServer=new VsphereServer(vsphereUrl, vsphereUsername,vspherePassword);
		try {
			vsphereServer.connect();
						
			//Find if vm by name vmname already exists
			VirtualMachine existingVm=vsphereServer.findVmByName(vmName);

			if (existingVm!=null) {
				vsphereServer.vncActivateVm(existingVm,vncPort,vncPassword);
			} else {
				System.out.println("VM "+vmName+" not found");
			}


			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
