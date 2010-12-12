package be.jedi.jvspherecontrol.commands;

import be.jedi.jvspherecontrol.vsphere.VsphereServer;

import com.vmware.vim25.mo.VirtualMachine;

public class PowerOnVmCommand extends VsphereCommand  {

	private String vmName;


	public PowerOnVmCommand() {
		super();
	}


	public void execute() {

		try {
			VsphereServer vsphereServer=new VsphereServer(vsphereUrl, vsphereUsername,vspherePassword);
			vsphereServer.connect();

			//Find if vm by name vmname already exists
			VirtualMachine existingVm=vsphereServer.findVmByName(vmName);

			if (existingVm!=null) {
				vsphereServer.powerOnVm(existingVm);		
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
