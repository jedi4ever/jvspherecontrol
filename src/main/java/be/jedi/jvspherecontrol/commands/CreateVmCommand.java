package be.jedi.jvspherecontrol.commands;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvspherecontrol.JVsphereControl;
import be.jedi.jvspherecontrol.VmDisk;
import be.jedi.jvspherecontrol.VmNic;
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
	public String vmOsType;
	public long vmMemory;
	
	public int vmCpus=1;
	
	public String vsphereDataStoreName="datastore1";
	public String vsphereDataCenterName="ha-datacenter";
	
	public VmNic[] vmNics;	
	public VmDisk[] vmDisks;	

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
	
	 public String getKeyword() {
			return keyword;
		}

		 public String getDescription() {
			return description;
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
				System.err.println("datastore "+vsphereDataStoreName+" does not exist");
				System.exit(-1);				
			}

			//Check datacenter
			if (!datacenters.contains(vsphereDataCenterName)) {
				System.err.println("datacenter "+vsphereDataCenterName+" does not exist");
				System.exit(-1);				
			}

			//Check networks to be used
			for (int i=0; i< vmNics.length; i++) {
				if (! networks.contains(vmNics[i].getNetwork())) {
					System.err.println("network "+vmNics[i].getNetwork()+" does not exist");
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
					System.out.println("use overwrite=true to overwrite");
					System.exit(1);
				}
			} 

			VirtualMachine newVm=vsphereServer.createVm(vmName,vmMemory,
					vmCpus,vmOsType,vmDisks,
					vmNics,vsphereDataCenterName,vsphereDataStoreName);

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
			
			vsphereServer.setBootOrderVm(newVm, "allow:net");

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
		vmName=cmdLine.getOptionValue("name");
		if (vmName.length()<=1) {
			throw new InvalidCLIArgumentSyntaxException("name must be at least two characters");
		}
	
		//Number of CPU's for the new VM
		String vmCpusString=cmdLine.getOptionValue("cpus");
		if (vmCpusString!=null) {
			try {
				this.vmCpus=Integer.parseInt(vmCpusString);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("vmCpuCount must be an integer");
			}
		} else {
			JVsphereControl.logger.debug("no vmCpuCount is given, using default value of "+vmCpus+" for vmCpuCount");
		}
		
		//Memorysize of the new VM
		String vmMemoryString=cmdLine.getOptionValue("memory");
		if (vmMemoryString!=null) {
			try {
				this.vmMemory=Integer.parseInt(vmMemoryString);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("memory must be an integer");
			}
		} else {
			JVsphereControl.logger.debug("no memory is given, using default value of "+vmMemory+" for memory");
		}
		
		
		///Option GuesOsId - http://www.vmware.com/support/developer/windowstoolkit/wintk40u1/html/Set-VM.html
		//we need to check this against a list!
		vmOsType=cmdLine.getOptionValue("ostype");

		//Overwrite the VM or not 
		String vmOverwriteString=cmdLine.getOptionValue("overwrite");
		if (vmOverwriteString!=null) {
			vmOverwrite=Boolean.parseBoolean(vmOverwriteString);	
		}

		//Register the machine in Omapi
		String vmOmapiRegisterString=cmdLine.getOptionValue("omapiregister");
		if (vmOmapiRegisterString!=null) {
			vmOmapiRegister=Boolean.parseBoolean(vmOmapiRegisterString);	
		}
		
		//Omapi Host
		omapiHost=cmdLine.getOptionValue("omapihost");
		if (omapiHost!=null) {
			if (omapiHost.length()<2) {
				throw new InvalidCLIArgumentSyntaxException("omapihost must be at least two characters");				
			} else {
				//nothing to do here
			}
		} else {
			if (vmOmapiRegister) {
			throw new MissingCLIArgumentException("omapihost is required when omapiregister is enabled");	
			}
		}

		//Omapi Port : default 9991
		String omapiPortString=cmdLine.getOptionValue("omapiport");
		if (omapiPortString!=null) {
			try {
				this.omapiPort=Integer.parseInt(omapiPortString);
			} catch (NumberFormatException ex) {
				throw new InvalidCLIArgumentSyntaxException("omapiport must be an integer");
			}
		} else {
			JVsphereControl.logger.debug("no omapiport is given, using default value of "+omapiPort+" for omapiport");
		}	
				
		omapiKeyName=cmdLine.getOptionValue("omapikeyname");
		omapiKeyValue=cmdLine.getOptionValue("omapikeyvalue");
		
		if (vmOmapiRegister) {
			if (omapiKeyName==null) {
				throw new MissingCLIArgumentException("omapikeyname is required when omapiregister is enabled");				
			}
			if (omapiKeyValue==null) {
				throw new MissingCLIArgumentException("omapikeyvalue is required when omapiregister is enabled");				
			}
		}
		
		//Omapi Overwrite
		String omapiOverWriteString=cmdLine.getOptionValue("omapioverwrite");
		if (omapiOverWriteString!=null) {
			omapiOverWrite=Boolean.parseBoolean(omapiOverWriteString);	
		}		
		

		//We need to check this against the available datacenter and datastores
		vsphereDataCenterName=cmdLine.getOptionValue("datacenter");
		vsphereDataStoreName=cmdLine.getOptionValue("datastore");
		
		
		/*****  Disks *****/	
		//Size of the disk to be created
		int lastDisk=0;
		for (int i=1; i< 20; i++) {
			if (cmdLine.getOptionValues("disksize"+i)==null) {
				break;
			} else {
				lastDisk++;
			}
		}

		vmDisks=new VmDisk[lastDisk];
		for (int i=0; i< vmDisks.length; i++) {

			String disk_modes_values[]={ "persistent","independent_persistent","independent_nonpersistent"};	

			String diskSize=cmdLine.getOptionValue("disksize"+(i+1));
			String diskMode=cmdLine.getOptionValue("diskmode"+(i+1));
		
			boolean vmDiskModeMatch=false;
			for (int j=0; j< disk_modes_values.length; j++) {
				if (disk_modes_values[j].equals(diskMode)) {
					vmDiskModeMatch=true;
				}
			}
			if (!vmDiskModeMatch) {
				throw new InvalidCLIArgumentSyntaxException("diskmode must be persistent,independent_persistent,idependent_nonpersistent");
			}
			
			VmDisk vmDisk=new VmDisk();
			
			vmDisk.setSize(Long.parseLong(diskSize));
			vmDisk.setMode(diskMode);
			vmDisks[i]=vmDisk;
		}

		
		/*****  Network *****/		
		//Network	
		vmPxeInterface=cmdLine.getOptionValue("pxeinterface");
		
		int lastNic=0;
		for (int i=1; i< 20; i++) {
			if (cmdLine.getOptionValues("nicname"+i)==null) {
				break;
			} else {
				lastNic++;
			}
		}
		
		vmNics=new VmNic[lastNic];
		
		for (int i=0; i< vmNics.length; i++) {
			String nicName=cmdLine.getOptionValue("nicname"+(i+1));
//			String nicType=cmdLine.getOptionValue("nictype"+(i+1));
			String nicNetwork=cmdLine.getOptionValue("nicnetwork"+(i+1));
			VmNic vmNic=new VmNic();
			vmNic.setName(nicName);
//			vmNic.setType(nicType);
			vmNic.setNetwork(nicNetwork);
			vmNics[i]=vmNic;
		}
		
		
		//CDROM
		vmCdromIsoFile=cmdLine.getOptionValue("cdromisofile");
		vmCdromDataStoreName=cmdLine.getOptionValue("cdromdatastore");

	}
	


	void initOptions() {
		super.initOptions();

		Option vmName=OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of vm to create" ).create( "name" );
		vmName.setRequired(true);
		options.addOption(vmName);

		Option vmMemorySize=OptionBuilder.withArgName( "size in MB" ).hasArg().withDescription(  "memory size to allocate" ).create( "memory" );
		vmMemorySize.setRequired(true);
		options.addOption(vmMemorySize);


		Option vmOsType=OptionBuilder.withArgName( "guestOsId" ).hasArg().withDescription(  "type of vm to create" ).create( "ostype" );
		vmOsType.setRequired(true);
		options.addOption(vmOsType);

		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of the datacenter to store new Vm" ).create( "datacenter" ));
		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of the datastore to store new Vm" ).create( "datastore" ));


		options.addOption(OptionBuilder.withArgName( "count" ).hasArg().withDescription(  "number of cpu's to allocate" ).create( "cpus" ));

		
		options.addOption(OptionBuilder.withArgName( "filename" ).hasArg().withDescription(  "dvd isofile" ).create( "cdromisofile" ));
		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "dvd datastorename" ).create( "cdromdatastore" ));		

		for (int i=1;i < 20; i++) {
			options.addOption(OptionBuilder.withArgName( "name" ).hasArgs().withDescription(  "name of the Nic interface" ).create( "nicname"+i ));
			options.addOption(OptionBuilder.withArgName( "type" ).hasArgs().withDescription(  "type of the Nic interface" ).create( "nictype"+i ));
			options.addOption(OptionBuilder.withArgName( "network" ).hasArgs().withDescription(  "network of the Nic interface" ).create( "nicnetwork"+i ));

			options.addOption(OptionBuilder.withArgName( "size" ).hasArgs().withDescription(  "size in kb of disk to create" ).create( "disksize"+i ));
			options.addOption(OptionBuilder.withArgName( "persistent|independent_persistent|independent_nonpersistent" ).hasArgs().withDescription(  "disk mode" ).create( "diskmode"+i ));
		
		}
		
		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("overwrite vm Flag").create("overwrite"));		

		options.addOption(OptionBuilder.withArgName( "interfacename" ).hasArg().withDescription(  "name of the network interface to PXE from" ).create( "pxeinterface" ));

		
		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("register with omapi server").create("omapiregister"));	
		options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("overwrite omapi entry").create("omapioverwrite"));		
		options.addOption(OptionBuilder.withArgName("hostname").hasArg().withDescription("omapi hostname").create("omapihost"));		
		options.addOption(OptionBuilder.withArgName("port").hasArg().withDescription("omapi portname").create("omapiport"));		
		options.addOption(OptionBuilder.withArgName("keyname").hasArg().withDescription("omapi key to use").create("omapikeyname"));		
		options.addOption(OptionBuilder.withArgName("base64 string").hasArg().withDescription("omapi value").create("omapikeyvalue"));		
		
	}
}
