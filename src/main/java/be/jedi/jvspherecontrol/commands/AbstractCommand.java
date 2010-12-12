package be.jedi.jvspherecontrol.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public  class AbstractCommand {


	public static String keyword="abstract command"; 
	public static String description="you should never see this"; 
	CommandLine cmdLine;

	public String getKeyword() {
		return keyword;
	}

	public String getDescription() {
		return description;
	}
	

	String args[]; 
	
	ArgsConfig config;
	Options options=new Options();
	


	public AbstractCommand() {
		initOptions();
	}

	public void init(String args[]) {
		this.args=args;
	}


	public void validateArgs() {
		
		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			cmdLine = parser.parse( options, args );
		}
		catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		}
		
	}
	
	public void execute() {
		printHelp();
	}

	public void printHelp() {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "java -jar jvspherecontrol.jar "+this.getKeyword()+" ", options );
		
	}

	void initOptions() {
			
	}


}
