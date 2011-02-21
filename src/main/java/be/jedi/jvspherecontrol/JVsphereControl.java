
package be.jedi.jvspherecontrol;

import java.util.ArrayList;
import java.util.Arrays;

import be.jedi.jvspherecontrol.commands.*;
import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.InvalidCLICommandException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JVsphereControl {

	public static Logger logger=Logger.getLogger(JVsphereControl.class);

	ArrayList<AbstractCommand> commands=new ArrayList<AbstractCommand>();
	Options options=new Options();
	CommandLine cmdLine;

	@SuppressWarnings("rawtypes")
	String[] classlist = { 
		"be.jedi.jvspherecontrol.commands.ListVsphereCommand",
		"be.jedi.jvspherecontrol.commands.CreateVmCommand",
		"be.jedi.jvspherecontrol.commands.GuestCommand", 
		"be.jedi.jvspherecontrol.commands.OmapiRegisterCommand"
		//			ActivateVncInVmCommand.class,
		//			SendVncTextCommand.class,
	};

	String mainArgs[];
	String commandString;

	public JVsphereControl(String[] args) {

		mainArgs=args;
		loadCommands();
	}

	public static void main(String[] args) {

		JVsphereControl jmachineControl=new JVsphereControl(args);

		try {
			jmachineControl.validateArgs();
			jmachineControl.execute();
		} catch (Exception e) {
			jmachineControl.printHelp();
			System.err.println("Error: "+e.getMessage());
			System.exit(-1);
		}
	}


	Integer execute() {
		AbstractCommand command=getCommandByString(commandString);

		if (command!=null) {
			System.out.println("executing");
			command.execute();
		}
		return (0);
	}

	void validateArgs() throws MissingCLIArgumentException,InvalidCLICommandException, InvalidCLIArgumentSyntaxException {

		String commandArguments[]={};
		boolean helpRequest=false;
		boolean debugRequest=false;
	
		//We need at least one argument
		if (mainArgs.length>0) {

			//check for help or debug
			ArrayList<String> argsList=new ArrayList<String>(Arrays.asList(mainArgs));
			
			if (argsList.contains("--help")) {
				helpRequest=true;
			}

			if (argsList.contains("--debug")) {
				BasicConfigurator.configure();
				logger.setLevel(Level.DEBUG);
				debugRequest=true;
			}
			
			//strip of args with --
			ArrayList<String> filteredArgsList=new ArrayList<String>();
			ArrayList<String> commandArgsList=new ArrayList<String>();

			int nonOptionCounter=0;
			for (String a : argsList) {
				//for all plain arguments , no options
				if (!a.startsWith("-")) {
					filteredArgsList.add(a);

					//If this is the second or more option add it to the commandArgs
					nonOptionCounter++;
					if (!(nonOptionCounter==1))
					{
						commandArgsList.add((String)a);
					}

				} else {
					commandArgsList.add((String)a);					
				}

			}

			
			//The first argument is the commandString
			commandString=filteredArgsList.get(0);

			if (helpRequest)
			
			//The commandString has to match a command
			if (!availableCommands().contains(commandString)) {
				printHelp();
				//throw new InvalidCLICommandException();
			} else {
				if (helpRequest) {
					printHelp(commandString);
				}
			}

			//We need to filter out the command and pass the rest
			
			//The rest of the arguments are the commandArguments
			if (mainArgs.length>1) {
				commandArguments=new String[commandArgsList.size()];
				System.arraycopy(commandArgsList.toArray(), 0, commandArguments, 0,commandArgsList.size());

			} else {
				commandArguments=null;
				//we should print the commands and say <command> help
				printHelp();
			}

			//prepare a command
			try {
				AbstractCommand command=getCommandByString(commandString);
				if (command==null)  {
					//Not a valid command
					InvalidCLIArgumentSyntaxException ex=new InvalidCLIArgumentSyntaxException("Unknown command:"+commandString);
					throw ex;
				}
				command.init(commandArguments);
				command.validateArgs();

			} catch (InvalidCLIArgumentSyntaxException e) {
				logger.error(e.toString());
				InvalidCLIArgumentSyntaxException ex=new InvalidCLIArgumentSyntaxException("Syntax error in argument:"+e.getLocalizedMessage());
				throw ex;				
			}
		} else {
			logger.error("We need at least one arg");
			MissingCLIArgumentException ex=new MissingCLIArgumentException("We need at least one argument");
			throw ex;
		}	
		
	}


	// Retrieves the correct command from the commands list by commandString
	AbstractCommand getCommandByString(String commandString) {
		AbstractCommand command=null;

		for (int c=0; c< commands.size(); c++ ) {
			if (commands.get(c).getKeyword().equals(commandString)) {
				command=commands.get(c);
			}
		}
		return command;
	}

	// This returns a string list with the commandString available
	ArrayList<String> availableCommands() {

		ArrayList<String> commandStringList=new ArrayList<String>();

		for (int c=0; c< commands.size(); c++ ) {
			commandStringList.add(commands.get(c).getKeyword());
		}
		logger.debug(commandStringList.toString());
		return commandStringList;
	}

	// This function tries to load the varies command-plugins and registers them
	void loadCommands() {

		AbstractCommand command;
		logger.info("about to load the plugins");
		for (int c=0; c < classlist.length; c++) {
			try {
				logger.debug("loading plugin :"+classlist[c]);
				command = (AbstractCommand) Class.forName(classlist[c]).newInstance();
				commands.add(command);
			} catch (InstantiationException e) {
				logger.error("failed to instantiate command "+classlist[c]);
			} catch (IllegalAccessException e) {
				logger.error("illegal access to command "+classlist[c]);
			} catch (ClassNotFoundException e) {
				logger.error("plugin class not found for command "+classlist[c]);
			}
		}
	}	
	
	void printHelp() {
		printHelp(null);
	}
	void printHelp(String commandString){
			
		System.out.println("jvspherecontrol v0.1");
		System.out.println("================================================");
		for (int c=0; c< commands.size(); c++ ) {
			if (commandString!=null) {
				if (commands.get(c).getKeyword().equals(commandString)) {
					System.out.println(commandString+"     "+commands.get(c).getHelp());
				}
			} else {
				System.out.println(commands.get(c).getKeyword()+"     "+commands.get(c).getHelp());
				
			}
			
		}
		

	}

}