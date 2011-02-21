package be.jedi.jvspherecontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.talamonso.OMAPI.Exceptions.OmapiConnectionException;
import org.talamonso.OMAPI.Exceptions.OmapiInitException;

import be.jedi.jvspherecontrol.JVsphereControl;
import be.jedi.jvspherecontrol.dhcp.OmapiServer;
import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;

public class OmapiRegisterCommand extends AbstractCommand  {

	
	public static String keyword="omapiregister"; 
	public static String description="registers a macaddress in omapi";
	
	public boolean omapiOverWrite=false;

	public String omapiKeyValue;
	public int omapiPort=9991;
	public String omapiKey;
	public String omapiHost;
	public String omapiKeyName;
	boolean vmOmapiRegister=true;
	public String macaddress="";
	public String hostname="";
	
	public OmapiRegisterCommand() {
		super();
	}


	 public String getKeyword() {
			return keyword;
	}

	public String getDescription() {
			return description;
	}
	
	public void execute() {
			
		if (vmOmapiRegister) {
			OmapiServer omapiServer=new OmapiServer(omapiHost,omapiPort,omapiKeyName, omapiKeyValue);
			String macAddress=macaddress;
			try {
				omapiServer.updateDHCP(hostname, macAddress,omapiOverWrite);
			} catch (OmapiInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OmapiConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(macAddress);
		}

	}
	
	
	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException {
		
		super.validateArgs();
		
		macaddress=cmdLine.getOptionValue("macaddress");
		hostname=cmdLine.getOptionValue("hostname");
		
		//Omapi Host
		omapiHost=cmdLine.getOptionValue("omapihost");
		if (omapiHost!=null) {
			if (omapiHost.length()<2) {
				throw new InvalidCLIArgumentSyntaxException("omapihost must be at least two characters");				
			} else {
				//nothing to do here
			}
		} else {
			if (vmOmapiRegister) {
			throw new MissingCLIArgumentException("omapihost is required when omapiregister is enabled");	
			}
		}

		//Omapi Port : default 9991
		String omapiPortString=cmdLine.getOptionValue("omapiport");
		if (omapiPortString!=null) {
			try {
				this.omapiPort=Integer.parseInt(omapiPortString);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("omapiport must be an integer");
			}
		} else {
			JVsphereControl.logger.debug("no omapiport is given, using default value of "+omapiPort+" for omapiport");
		}	
				
		omapiKeyName=cmdLine.getOptionValue("omapikeyname");
		omapiKeyValue=cmdLine.getOptionValue("omapikeyvalue");
		
		
		if (vmOmapiRegister) {
			if (omapiKeyName==null) {
				throw new MissingCLIArgumentException("omapikeyname is required when omapiregister is enabled");				
			}
			if (omapiKeyValue==null) {
				throw new MissingCLIArgumentException("omapikeyvalue is required when omapiregister is enabled");				
			}
		}
		
		//Omapi Overwrite
		String omapiOverWriteString=cmdLine.getOptionValue("omapioverwrite");
		if (omapiOverWriteString!=null) {
			omapiOverWrite=Boolean.parseBoolean(omapiOverWriteString);	
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
	
	
	void initOptions() {
		super.initOptions();

		Option macaddress=OptionBuilder.withArgName( "macaddress" ).hasArg().withDescription(  "mac address to register" ).create( "macaddress" );
		macaddress.setRequired(true);
		options.addOption(macaddress);
		
		Option hostname=OptionBuilder.withArgName( "hostname" ).hasArg().withDescription(  "hostname to register" ).create( "hostname" );
		hostname.setRequired(true);
		options.addOption(hostname);
		
		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("register with omapi server").create("omapiregister"));	
		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("overwrite omapi entry").create("omapioverwrite"));		
		options.addOption(OptionBuilder.withArgName("hostname").hasArg().withDescription("omapi hostname").create("omapihost"));		
		options.addOption(OptionBuilder.withArgName("port").hasArg().withDescription("omapi portname").create("omapiport"));		
		options.addOption(OptionBuilder.withArgName("keyname").hasArg().withDescription("omapi key to use").create("omapikeyname"));		
		options.addOption(OptionBuilder.withArgName("base64 string").hasArg().withDescription("omapi value").create("omapikeyvalue"));		
		
	}

}
