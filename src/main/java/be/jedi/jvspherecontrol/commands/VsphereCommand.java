package be.jedi.jvspherecontrol.commands;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;


public class VsphereCommand extends AbstractCommand  {

	public static String keyword="vpsherecommand"; 
	public static String description="this is an abstract class a virtual machine"; 
	
	
	String vsphereUrl, vsphereUsername,vspherePassword;	
	
	public VsphereCommand() {
		super();
	}

	void initOptions() {
		super.initOptions();

		Option help = new Option( "help", "print this message" ); options.addOption(help);

		Option vsphereUrl=OptionBuilder.withArgName( "vsphereUrl" ).hasArg().withDescription(  "url to connect to" ).create( "vsphereUrl" );
		vsphereUrl.setRequired(true);
		options.addOption(vsphereUrl);

		Option vsphereUserName=OptionBuilder.withArgName( "vsphereUsername" ).hasArg().withDescription(  "username to connect to vSphere" ).create( "vsphereUserName" );
		vsphereUserName.setRequired(true);
		options.addOption(vsphereUserName);

		Option vsphereUserPassword=OptionBuilder.withArgName( "vspherePassword" ).hasArg().withDescription(  "password to connect to vSphere" ).create( "vspherePassword" );
		vsphereUserPassword.setRequired(true);
		options.addOption(vsphereUserPassword);

	}
	
	public void init(String args[]) {
		super.init(args);
	}

	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException {
		
		super.validateArgs();

		vsphereUrl=cmdLine.getOptionValue("vsphereUrl");
		
		try {
			URI uri= new URI (vsphereUrl);
		} catch (URISyntaxException e) {
			throw new InvalidCLIArgumentSyntaxException("Vsphere Url has an invalid URL syntax");
		}
		
		vsphereUsername=cmdLine.getOptionValue("vsphereUserName");
		vspherePassword=cmdLine.getOptionValue("vspherePassword");
		
	}
	
}
