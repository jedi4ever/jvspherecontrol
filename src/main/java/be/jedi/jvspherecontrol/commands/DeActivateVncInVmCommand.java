package be.jedi.jvspherecontrol.commands;

import java.lang.reflect.Array;

import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvspherecontrol.dhcp.OmapiServer;
import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;
import be.jedi.jvspherecontrol.vsphere.VsphereServer;

import com.vmware.vim25.mo.VirtualMachine;

public class DeActivateVncInVmCommand extends VsphereCommand  {

	public static String keyword="deactivateVncInVm"; 
	public static String description="this creates a virtual machine";
	
	public String vmName;
	
	public String vncPassword;
	public int vncPort;
		
	public DeActivateVncInVmCommand() {
		super();
	}

	public void init(String args[]) {
		super.init(args);
	}

	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException{
		
		super.validateArgs();
		
		vmName=cmdLine.getOptionValue("vmName");
	}
	
	void initOptions() {
		super.initOptions();

		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of vm to create" ).create( "vmName" ));
	}
	
	
	public void execute(){
		
		VsphereServer vsphereServer=new VsphereServer(vsphereUrl, vsphereUsername,vspherePassword);
		try {
			vsphereServer.connect();
						
			//Find if vm by name vmname already exists
			VirtualMachine existingVm=vsphereServer.findVmByName(vmName);

			if (existingVm!=null) {
				vsphereServer.vncDeActivateVm(existingVm);
			} else {
				System.out.println("VM "+vmName+" not found");
			}


			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
