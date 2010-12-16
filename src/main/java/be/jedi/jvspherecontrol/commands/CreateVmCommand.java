package be.jedi.jvspherecontrol.commands;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvspherecontrol.JVsphereControl;
import be.jedi.jvspherecontrol.dhcp.OmapiServer;
import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;
import be.jedi.jvspherecontrol.vsphere.VsphereServer;

import com.vmware.vim25.mo.VirtualMachine;

public class CreateVmCommand extends VsphereCommand  {

	public static String keyword="createvm"; 
	public static String description="this creates a virtual machine";
	
	public boolean vmOverwrite=false;

	public String vmName;
	public String vmGuestOsId;
	public long vmDiskSize=10000;
	public long vmMemorySize;
	public String vmDiskMode="persistent";
	
	public int vmCpuCount=1;
	
	public String vsphereDataStoreName="datastore1";
	public String vsphereDataCenterName="ha-datacenter";
	
	public String[][] vmInterfaces;	
	public String[][] vmDisks;	

	public String vmCdromIsoFile;
	public String vmCdromDataStoreName;
	
	public String vmPxeInterface;
	public boolean vmVncActivate;	
	public String vmVncPassword;
	public int vmVncPort;
	
	public boolean vmOmapiRegister=false;
	public boolean omapiOverWrite=false;

	public String omapiKeyValue;
	public int omapiPort=9991;
	public String omapiKey;
	public String omapiHost;
	public String omapiKeyName;	
	
	public CreateVmCommand() {
		super();
	}

	public void init(String args[]) {
		super.init(args);
	}

	public void execute(){
		
		VsphereServer vsphereServer=new VsphereServer(vsphereUrl, vsphereUsername,vspherePassword);
		try {
			vsphereServer.connect();
						

			ArrayList<String> datacenters=vsphereServer.listDataCenters();
			ArrayList<String> datastores=vsphereServer.listDataStores();
			ArrayList<String> networks=vsphereServer.listNetworks();
			
			//Check datastore
			if (!datastores.contains(vsphereDataStoreName)) {
				System.err.println("vsphereDataStoreName "+vsphereDataStoreName+" does not exist");
				System.exit(-1);				
			}

			//Check datacenter
			if (!datacenters.contains(vsphereDataCenterName)) {
				System.err.println("vsphereDataCenterName "+vsphereDataCenterName+" does not exist");
				System.exit(-1);				
			}

			//Check networks to be used
			for (int i=0; i< vmInterfaces.length; i++) {
				if (! networks.contains(vmInterfaces[i][0])) {
					System.err.println("network "+vmInterfaces[i][0]+" does not exist");
					System.exit(-1);
				}
			}

			//verify cdrom 
			
			//Find if vm by name vmname already exists
			VirtualMachine existingVm=vsphereServer.findVmByName(vmName);

			if (existingVm!=null) {
				System.out.println("Machine exists with name:"+existingVm.getName());
				//vsphereServer.showVmDetail(existingVm);

				//Where was this used for?
				//			VirtualMachineCapability vmc = existingVm.getCapability();
				//			VirtualMachineSummary summary = (VirtualMachineSummary) (existingVm.getSummary());

				if(vmOverwrite) {
					if (vsphereServer.isVmPoweredOn(existingVm))
					{
						vsphereServer.powerOffVm(existingVm);	  		
					} 

					vsphereServer.destroyVm(existingVm);	  		

				} else {
					System.out.println("use vmOverwrite=true to overwrite");
					System.exit(1);
				}
			} 

			VirtualMachine newVm=vsphereServer.createVm(vmName,vmMemorySize,
					vmCpuCount,vmGuestOsId,vmDisks,
					vmInterfaces,vsphereDataCenterName,vsphereDataStoreName);

			if (vmCdromIsoFile!=null) {
				vsphereServer.setCdromVm(newVm,vmCdromIsoFile,vmCdromDataStoreName);
				
			}

			if (vmVncActivate) {
				vsphereServer.vncActivateVm(newVm,vmVncPort,vmVncPassword);
			}

			if (vmPxeInterface!=null) {
				vsphereServer.setVmPxebootInterface(newVm,vmPxeInterface);
			}

			//Enough config, let's prepare the server for it's first boot
			vsphereServer.setEnterBiosVm(newVm,true);

			if (newVm!=null) {
				vsphereServer.powerOnVm(newVm);
			}

			//it is now in bios waiting , so we can power it off
			if (newVm!=null) {
				vsphereServer.powerOffVm(newVm);
			}

			//flip the enterbios flag
			vsphereServer.setEnterBiosVm(newVm,false);
			
			vsphereServer.setBootOrderVm(newVm, "allow:cd");

			//vsphereServer.listNicsVm(newVm);
			if (vmOmapiRegister) {
				System.out.println("registering nic:"+vmPxeInterface);

				OmapiServer omapiServer=new OmapiServer(omapiHost,omapiPort,omapiKeyName, omapiKeyValue);
				String macAddress=vsphereServer.getMacAddress(vmPxeInterface,newVm);
				omapiServer.updateDHCP(vmName, macAddress,omapiOverWrite);
				System.out.println(macAddress);
			}

			vsphereServer.powerOnVm(newVm);		
		} catch (RemoteException ex) {	
			System.err.println("eror logging in the vsphere, username, password?");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void validateArgs() throws MissingCLIArgumentException, InvalidCLIArgumentSyntaxException{
		
		super.validateArgs();
		
		//Name of the Virtual Machine
		vmName=cmdLine.getOptionValue("vmName");
		if (vmName.length()<=1) {
			throw new InvalidCLIArgumentSyntaxException("vmName must be at least two characters");
		}
			

	
		//Number of CPU's for the new VM
		String vmCpuCountString=cmdLine.getOptionValue("vmCpuCount");
		if (vmCpuCountString!=null) {
			try {
				this.vmCpuCount=Integer.parseInt(vmCpuCountString);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("vmCpuCount must be an integer");
			}
		} else {
			JVsphereControl.logger.debug("no vmCpuCount is given, using default value of "+vmCpuCount+" for vmCpuCount");
		}
		
		//Memorysize of the new VM
		String vmMemorySizeString=cmdLine.getOptionValue("vmMemorySize");
		if (vmMemorySizeString!=null) {
			try {
				this.vmMemorySize=Integer.parseInt(vmMemorySizeString);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("vmMemorySize must be an integer");
			}
		} else {
			JVsphereControl.logger.debug("no vmMemorySize is given, using default value of "+vmMemorySize+" for vmMemorySize");
		}
		
		
		///Option GuesOsId - http://www.vmware.com/support/developer/windowstoolkit/wintk40u1/html/Set-VM.html
		//we need to check this against a list!
		vmGuestOsId=cmdLine.getOptionValue("vmGuestOsId");

		//Overwrite the VM or not 
		String vmOverwriteString=cmdLine.getOptionValue("vmOverWrite");
		if (vmOverwriteString!=null) {
			vmOverwrite=Boolean.parseBoolean(vmOverwriteString);	
		}

		//Register the machine in Omapi
		String vmOmapiRegisterString=cmdLine.getOptionValue("vmOmapiRegister");
		if (vmOmapiRegisterString!=null) {
			vmOmapiRegister=Boolean.parseBoolean(vmOmapiRegisterString);	
		}
		
		//Omapi Host
		omapiHost=cmdLine.getOptionValue("omapiHost");
		if (omapiHost!=null) {
			if (omapiHost.length()<2) {
				throw new InvalidCLIArgumentSyntaxException("omapiHost must be at least two characters");				
			} else {
				//nothing to do here
			}
		} else {
			if (vmOmapiRegister) {
			throw new MissingCLIArgumentException("omapiHost is required when vmOmapiRegister is enabled");	
			}
		}

		//Omapi Port : default 9991
		String omapiPortString=cmdLine.getOptionValue("omapiPort");
		if (omapiPortString!=null) {
			try {
				this.omapiPort=Integer.parseInt(omapiPortString);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("omapiPort must be an integer");
			}
		} else {
			JVsphereControl.logger.debug("no omapiPort is given, using default value of "+omapiPort+" for omapiPort");
		}	
				
		omapiKeyName=cmdLine.getOptionValue("omapiKeyName");
		omapiKeyValue=cmdLine.getOptionValue("omapiKeyValue");
		
		if (vmOmapiRegister) {
			if (omapiKeyName==null) {
				throw new MissingCLIArgumentException("omapiKeyName is required when vmOmapiRegister is enabled");				
			}
			if (omapiKeyValue==null) {
				throw new MissingCLIArgumentException("omapiKeyValue is required when vmOmapiRegister is enabled");				
			}
		}
		
		//Omapi Overwrite
		String omapiOverWriteString=cmdLine.getOptionValue("omapiOverWrite");
		if (omapiOverWriteString!=null) {
			omapiOverWrite=Boolean.parseBoolean(omapiOverWriteString);	
		}		
		
		
		
		/*****  Disks *****/	
		//Size of the disk to be created
		String[] diskSizes=cmdLine.getOptionValues("vmDiskSize");
		String[] diskModes=cmdLine.getOptionValues("vmDiskMode");
		
		vmDisks=new String[diskSizes.length][];
		for (int i=0; i< diskSizes.length; i++) {

			try {
				Integer.parseInt(diskSizes[i]);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("vmDiskSize must be an integer");
			}
			String disk_modes_values[]={ "persistent","independent_persistent","idependent_nonpersistent"};	
		
			boolean vmDiskModeMatch=false;
			for (int j=0; j< disk_modes_values.length; j++) {
				if (disk_modes_values[j].equals(diskModes[i])) {
					vmDiskModeMatch=true;
				}
			}
			if (!vmDiskModeMatch) {
				throw new InvalidCLIArgumentSyntaxException("vmDiskMode must be persistent,independent_persistent,idependent_nonpersistent");
			}
			
			vmDisks[i]=new String[]{ diskSizes[i],diskModes[i] };
		}
				

		//We need to check this against the available datacenter and datastores
		vsphereDataCenterName=cmdLine.getOptionValue("vsphereDataCenterName");
		vsphereDataStoreName=cmdLine.getOptionValue("vsphereDataStoreName");

		/*****  Network *****/		
		//Network	
		vmPxeInterface=cmdLine.getOptionValue("vmPxeInterface");
		
		
		String[] nicNames=cmdLine.getOptionValues("vmNicName");
//		String[] nicTypes=cmdLine.getOptionValues("vmNicType");
		String[] nicNetworks=cmdLine.getOptionValues("vmNicNetwork");
		
		vmInterfaces=new String[nicNames.length][];
		for (int i=0; i< nicNames.length; i++) {
			System.out.println(nicNetworks[i]);
			vmInterfaces[i]=new String[]{ nicNetworks[i],nicNames[i] };			
//			vmInterfaces[i]=new String[]{nicNames[i], nicNetworks[i], nicTypes[i] };			

		}
		
		//CDROM
		vmCdromIsoFile=cmdLine.getOptionValue("vmCdromIsoFile");
		vmCdromDataStoreName=cmdLine.getOptionValue("vmCdromDataStoreName");

	}
	


	void initOptions() {
		super.initOptions();

		Option vmName=OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of vm to create" ).create( "vmName" );
		vmName.setRequired(true);
		options.addOption(vmName);

		Option vmMemorySize=OptionBuilder.withArgName( "size in MB" ).hasArg().withDescription(  "memory size to allocate" ).create( "vmMemorySize" );
		vmMemorySize.setRequired(true);
		options.addOption(vmMemorySize);


		Option vmGuestOsId=OptionBuilder.withArgName( "guestOsId" ).hasArg().withDescription(  "type of vm to create" ).create( "vmGuestOsId" );
		vmGuestOsId.setRequired(true);
		options.addOption(vmGuestOsId);

		Option vmDiskSize=OptionBuilder.withArgName( "disksize" ).hasArgs().withDescription(  "size in kb of disk to create" ).create( "vmDiskSize" );
		vmDiskSize.setRequired(true);
		options.addOption(vmDiskSize);

		Option vmDiskMode=OptionBuilder.withArgName( "persistent|independent_persistent|independent_nonpersistent" ).hasArgs().withDescription(  "disk mode" ).create( "vmDiskMode" );
		vmDiskMode.setRequired(true);
		options.addOption(vmDiskMode);

		
		options.addOption(OptionBuilder.withArgName( "datacentername" ).hasArg().withDescription(  "name of the datacenter to store new Vm" ).create( "vsphereDataCenterName" ));
		options.addOption(OptionBuilder.withArgName( "datastorename" ).hasArg().withDescription(  "name of the datastore to store new Vm" ).create( "vsphereDataStoreName" ));


		options.addOption(OptionBuilder.withArgName( "cpucount" ).hasArg().withDescription(  "number of cpu's to allocate" ).create( "vmCpuCount" ));

		
		options.addOption(OptionBuilder.withArgName( "filename" ).hasArg().withDescription(  "dvd isofile" ).create( "vmCdromIsoFile" ));
		options.addOption(OptionBuilder.withArgName( "datastorename" ).hasArg().withDescription(  "dvd datastorename" ).create( "vmCdromDataStoreName" ));		


		options.addOption(OptionBuilder.withArgName( "name" ).hasArgs().withDescription(  "name of the Nic interface" ).create( "vmNicName" ));
		options.addOption(OptionBuilder.withArgName( "type" ).hasArgs().withDescription(  "type of the Nic interface" ).create( "vmNicType" ));
		options.addOption(OptionBuilder.withArgName( "network" ).hasArgs().withDescription(  "network of the Nic interface" ).create( "vmNicNetwork" ));

		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("overwrite vm Flag").create("vmOverWrite"));		

		options.addOption(OptionBuilder.withArgName( "interfacename" ).hasArg().withDescription(  "name of the network interface to PXE from" ).create( "vmPxeInterface" ));

		
		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("register with omapi server").create("vmOmapiRegister"));	
		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("overwrite omapi entry").create("omapiOverWrite"));		
		options.addOption(OptionBuilder.withArgName("hostname").hasArg().withDescription("omapi hostname").create("omapiHost"));		
		options.addOption(OptionBuilder.withArgName("port").hasArg().withDescription("omapi portname").create("omapiPort"));		
		options.addOption(OptionBuilder.withArgName("keyname").hasArg().withDescription("omapi key to use").create("omapiKeyName"));		
		options.addOption(OptionBuilder.withArgName("base64 string").hasArg().withDescription("omapi value").create("omapiKeyValue"));		
		
	}
}
