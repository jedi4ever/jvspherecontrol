package be.jedi.jvspherecontrol.commands;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvspherecontrol.dhcp.OmapiServer;
import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;
import be.jedi.jvspherecontrol.vsphere.VsphereServer;

import com.vmware.vim25.mo.VirtualMachine;

public class DeActivateVncInVmCommand extends VsphereCommand  {

	public static String keyword="deactivatevnc"; 
	public static String description="this disable vnc for a virtual machine";
	
	public String vmName;
	
	public String vncPassword;
	public int vncPort;
		
	public DeActivateVncInVmCommand() {
		super();
	}

	 public String getKeyword() {
			return keyword;
		}

		 public String getDescription() {
			return description;
		}
		 
	public void init(String args[]) {
		super.init(args);
	}

	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException{
		
		super.validateArgs();
		
		vmName=cmdLine.getOptionValue("vmname");
	}
	
	void initOptions() {
		super.initOptions();

		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "vmname to disable vnc" ).create( "vmname" ));
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
