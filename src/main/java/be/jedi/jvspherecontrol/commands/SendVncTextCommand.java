package be.jedi.jvspherecontrol.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvncsender.VncSender;
import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;



public class SendVncTextCommand extends AbstractCommand  {

	public static String keyword="sendvnctext"; 
	public static String description="send text strings to a vnc server";

	private boolean vncSendAction;
	private String vncHost;
	private int vncPort;
	private String vncPassword;
	private String[] vncText;

	public SendVncTextCommand() {
		super();	
	}
	
	 public String getKeyword() {
			return keyword;
		}

		 public String getDescription() {
			return description;
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
