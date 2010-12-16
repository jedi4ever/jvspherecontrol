package be.jedi.jvspherecontrol.commands;

import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvncsender.VncSender;
import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;



public class SendVncTextCommand extends AbstractCommand  {

	public static String keyword="sendVncText"; 
	public static String description="send text strings to a vnc server";

	private boolean vncSendAction;
	private String vncHost;
	private int vncPort;
	private String vncPassword;
	private String[] vncText;

	public SendVncTextCommand() {
		super();	
	}
	
	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException{
		
		super.validateArgs();
		vncText=cmdLine.getOptionValues("text");
		vncHost=cmdLine.getOptionValue("host");
		vncPassword=cmdLine.getOptionValue("password");
		vncPort=Integer.parseInt(cmdLine.getOptionValue("port"));
		
	}

	void initOptions() {
		options.addOption(OptionBuilder.withArgName( "hostname" ).hasArg().withDescription(  "host to send it to" ).create( "host" ));
		options.addOption(OptionBuilder.withArgName( "port" ).hasArg().withDescription(  "port to connect to" ).create( "port" ));
		options.addOption(OptionBuilder.withArgName( "password" ).hasArg().withDescription(  "password to use" ).create( "password" ));
		options.addOption(OptionBuilder.withArgName( "text" ).hasArgs().withDescription(  "text to send" ).create( "text" ));
		options.addOption(OptionBuilder.withArgName( "seconds" ).hasArg().withDescription( "seconds to wait in between sending different texts (default=1s)" ).create( "wait" ));

	}

	public void execute() {

		
		VncSender vncSender=new VncSender(vncHost,vncPort,vncPassword); 
		
		if (cmdLine.hasOption("wait")) {
			vncSender.setVncWaitTime(Integer.parseInt(cmdLine.getOptionValue("wait")));	
		}
		
		vncSender.sendText(vncText);	

	}

}
