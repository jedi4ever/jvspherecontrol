package be.jedi.jvspherecontrol.commands;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;


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

	ArrayList<Option> optionList=new ArrayList<Option>();
	
	Options options=new Options();
	

	public AbstractCommand() {
		initOptions();
	}

	public void init(String args[]) {
		this.args=args;
	}


	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException {
		
		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			cmdLine = parser.parse( options, args );
		}
		catch( ParseException exp ) {
			// oops, something went wrong
			throw new MissingCLIArgumentException("parse exception"+exp.getMessage());
		}
		
	}
	
	public void execute() {
		printHelp();
	}

	public void printHelp() {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		
		Iterator<Option> optionsIterator=options.getOptions().iterator();
		
		while (optionsIterator.hasNext()) {
			Option option=optionsIterator.next();
			System.out.println("Required:"+option.isRequired());
			System.out.println(option.getArgName());
			System.out.println(option.getArgs());
			System.out.println(option.getDescription());

		}
//		formatter.printHelp( "java -jar jvspherecontrol.jar "+this.getKeyword()+" ", options );
		
	}

	void initOptions() {
			
	}


}
