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

		Option vsphereUrl=OptionBuilder.withArgName( "url" ).hasArg().withDescription(  "url to connect to" ).create( "url" );
		vsphereUrl.setRequired(true);
		options.addOption(vsphereUrl);

		Option vsphereUserName=OptionBuilder.withArgName( "username" ).hasArg().withDescription(  "username to connect to vSphere" ).create( "user" );
		vsphereUserName.setRequired(true);
		options.addOption(vsphereUserName);

		Option vsphereUserPassword=OptionBuilder.withArgName( "password" ).hasArg().withDescription(  "password to connect to vSphere" ).create( "password" );
		vsphereUserPassword.setRequired(true);
		options.addOption(vsphereUserPassword);

	}
	
	public void init(String args[]) {
		super.init(args);
	}

	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException {
		
		super.validateArgs();

		vsphereUrl=cmdLine.getOptionValue("url");
		
		try {
			URI uri= new URI (vsphereUrl);
			if (!((uri.getScheme().equals("http")) || (uri.getScheme().equals("https")))) {
				throw new InvalidCLIArgumentSyntaxException("url has an invalid Scheme syntax: "+uri.getScheme());				
			}

		} catch (URISyntaxException e) {
			throw new InvalidCLIArgumentSyntaxException("url has an invalid URL syntax: "+e.getMessage());
		}
		
		
		vsphereUsername=cmdLine.getOptionValue("user");
		vspherePassword=cmdLine.getOptionValue("password");
		
	}
	
}
