package be.jedi.jvspherecontrol.commands;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;


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

		options.addOption(OptionBuilder.withArgName( "vsphereUrl" ).hasArg().withDescription(  "url to connect to" ).create( "vsphereUrl" ));
		options.addOption(OptionBuilder.withArgName( "vsphereUsername" ).hasArg().withDescription(  "username to connect to vSphere" ).create( "vsphereUserName" ));
		options.addOption(OptionBuilder.withArgName( "vspherePassword" ).hasArg().withDescription(  "password to connect to vSphere" ).create( "vspherePassword" ));

//		System.out.println(options.hasOption("vsphereUrl"));
//		System.out.println(options.getOption("vsphereUrl").getArgName());

	}
	
	public void init(String args[]) {
		super.init(args);
	}

	public void validateArgs() {
		super.validateArgs();
		vsphereUrl=cmdLine.getOptionValue("vsphereUrl");
		vsphereUsername=cmdLine.getOptionValue("vsphereUserName");
		vspherePassword=cmdLine.getOptionValue("vspherePassword");
	}
	
}
