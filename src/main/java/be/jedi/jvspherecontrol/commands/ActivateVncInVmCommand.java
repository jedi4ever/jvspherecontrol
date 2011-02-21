package be.jedi.jvspherecontrol.commands;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;


import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;
import be.jedi.jvspherecontrol.vsphere.VsphereServer;

import com.vmware.vim25.mo.VirtualMachine;

public class ActivateVncInVmCommand extends VsphereCommand  {

	public static String keyword="activatevnc"; 
	public static String description="this enables vnc on a virtual machine";
	
	public String vmName;
	
	public String vncPassword;
	public int vncPort;
	
	public String getKeyword() {
		return keyword;
	}

	public String getDescription() {
		return description;
	}
	
		
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

		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of vm to create" ).create( "vmname" ));
		options.addOption(OptionBuilder.withArgName( "portnumber" ).hasArg().withDescription(  "port to enable VNC on" ).create( "vncport" ));
		options.addOption(OptionBuilder.withArgName( "pqssword" ).hasArg().withDescription(  "password to set on the VNC" ).create( "vncpassword" ));
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
	
	public String getHelp() {

		String helpText="";
		ArrayList<String> sortedCommands=new ArrayList<String>();
		
		Iterator<Option> optionIterator=options.getOptions().iterator();
		while (optionIterator.hasNext()) {
			Option option=optionIterator.next();
			if (option.isRequired()) {
				
			}
			String optionName=option.getOpt();
			//If is a digit don't print if digit != 0
			Matcher matcher = Pattern.compile("\\d+").matcher(optionName);
			matcher.find();
			try {
				int n = Integer.valueOf(matcher.group());
				if (n==1) {
					sortedCommands.add("--"+option.getOpt()+"..n <"+option.getDescription()+">"+"\n");									
				}

			} catch (IllegalStateException ex) {
				sortedCommands.add("--"+option.getOpt()+" <"+option.getDescription()+">"+"\n");				
				
			}
		}
		Collections.sort(sortedCommands);
		
        for (String line : sortedCommands) {
        	helpText+=line;
         }
        
		return helpText;
	}


}
