package be.jedi.jvspherecontrol.commands;

public class KickstartVmCommand extends CreateVmCommand  {
	
	public static String keyword="kickstartvm"; 
	public static String description="this kickstarts a virtual machine";
	
	CreateVmCommand createVmCommand; 
	PowerOnVmCommand powerOnVmCommand;
	
	public KickstartVmCommand() {
		super();
		createVmCommand=new CreateVmCommand();

		powerOnVmCommand=new PowerOnVmCommand();

	}

	
	public void execute() {
		createVmCommand.execute();
		powerOnVmCommand.execute();	
		
	}

}
