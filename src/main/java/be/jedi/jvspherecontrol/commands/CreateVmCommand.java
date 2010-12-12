package be.jedi.jvspherecontrol.commands;

import java.lang.reflect.Array;

import org.apache.commons.cli.OptionBuilder;

import be.jedi.jvspherecontrol.dhcp.OmapiServer;
import be.jedi.jvspherecontrol.vsphere.VsphereServer;

import com.vmware.vim25.mo.VirtualMachine;

public class CreateVmCommand extends VsphereCommand  {

	public static String keyword="createvm"; 
	public static String description="this creates a virtual machine";
	
	public boolean vmOverwrite=false;

	public String vmName;
	public String vmGuestOsId;
	public long vmDiskSize;
	public long vmMemorySize;
	public String vmDiskMode;
	public int vmCpuCount;
	public String vsphereDataStoreName;
	public String vsphereDataCenterName;
	
	public String[][] vmInterfaces;	
	
	public String vmCdromIsoFile;
	public String vmCdromDataStoreName;
	
	public String vmPxeInterface;
	public boolean vmVncActivate;	
	public String vmVncPassword;
	public int vmVncPort;
	
	public boolean vmOmapiRegister=false;
	public boolean omapiOverWrite=false;

	public String omapiKeyValue;
	public int omapiPort;
	public String omapiKey;
	public String omapiHost;
	public String omapiKeyName;	
	
	public CreateVmCommand() {
		super();
	}

	public void init(String args[]) {
		super.init(args);
	}

	public void validateArgs(){
		
		super.validateArgs();
		
		vsphereDataCenterName=cmdLine.getOptionValue("vsphereDataCenterName");
		vsphereDataStoreName=cmdLine.getOptionValue("vsphereDataStoreName");
		vmName=cmdLine.getOptionValue("vmName");

		///Option GuesOsId - http://www.vmware.com/support/developer/windowstoolkit/wintk40u1/html/Set-VM.html
		vmGuestOsId=cmdLine.getOptionValue("vmGuestOsId");
		//"persistent|independent_persistent|independent_nonpersistent" 
		vmDiskSize=Integer.parseInt(cmdLine.getOptionValue("vmDiskSize"));
		
		System.out.println(vmDiskSize);
		
		vmCpuCount=Integer.parseInt(cmdLine.getOptionValue("vmCpuCount"));
		vmMemorySize=Integer.parseInt(cmdLine.getOptionValue("vmMemorySize"));

		vmCdromIsoFile=cmdLine.getOptionValue("vmCdromIsoFile");
		vmCdromDataStoreName=cmdLine.getOptionValue("vmCdromDataStoreName");

		vmDiskMode=cmdLine.getOptionValue("vmDiskMode");
		System.out.println(vmDiskMode);

		
		vmOverwrite=Boolean.parseBoolean(cmdLine.getOptionValue("vmOverWrite"));
		System.out.println(cmdLine.getOptionValue("vmOverWrite"));

		vmOmapiRegister=Boolean.parseBoolean(cmdLine.getOptionValue("vmOmapiRegister"));
		vmPxeInterface=cmdLine.getOptionValue("vmPxeInterface");

		omapiHost=cmdLine.getOptionValue("omapiHost");
		omapiPort=Integer.parseInt(cmdLine.getOptionValue("omapiPort"));
		
		omapiKeyName=cmdLine.getOptionValue("omapiKeyName");
		omapiKeyValue=cmdLine.getOptionValue("omapiKeyValue");
		omapiOverWrite=Boolean.parseBoolean(cmdLine.getOptionValue("omapiOverWrite"));

		
		String[] nicNames=cmdLine.getOptionValues("vmNicName");
//		String[] nicTypes=cmdLine.getOptionValues("vmNicType");
		String[] nicNetworks=cmdLine.getOptionValues("vmNicNetwork");

		vmInterfaces=new String[nicNames.length][];
		for (int i=0; i< nicNames.length; i++) {
			System.out.println(nicNetworks[i]);
			vmInterfaces[i]=new String[]{ nicNetworks[i],nicNames[i] };			
//			vmInterfaces[i]=new String[]{nicNames[i], nicNetworks[i], nicTypes[i] };			

		}

	}
	
	void initOptions() {
		super.initOptions();
		options.addOption(OptionBuilder.withArgName( "datacentername" ).hasArg().withDescription(  "name of the datacenter to store new Vm" ).create( "vsphereDataCenterName" ));
		options.addOption(OptionBuilder.withArgName( "datastorename" ).hasArg().withDescription(  "name of the datastore to store new Vm" ).create( "vsphereDataStoreName" ));

		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of vm to create" ).create( "vmName" ));
		options.addOption(OptionBuilder.withArgName( "guestOsId" ).hasArg().withDescription(  "type of vm to create" ).create( "vmGuestOsId" ));

		options.addOption(OptionBuilder.withArgName( "disksize" ).hasArg().withDescription(  "size in kb of disk to create" ).create( "vmDiskSize" ));
		options.addOption(OptionBuilder.withArgName( "cpucount" ).hasArg().withDescription(  "number of cpu's to allocate" ).create( "vmCpuCount" ));
		options.addOption(OptionBuilder.withArgName( "size in MB" ).hasArg().withDescription(  "memory size to allocate" ).create( "vmMemorySize" ));

		options.addOption(OptionBuilder.withArgName( "filename" ).hasArg().withDescription(  "dvd isofile" ).create( "vmCdromIsoFile" ));
		options.addOption(OptionBuilder.withArgName( "datastorename" ).hasArg().withDescription(  "dvd datastorename" ).create( "vmCdromDataStoreName" ));
		
		options.addOption(OptionBuilder.withArgName( "persistent|independent_persistent|independent_nonpersistent" ).hasArg().withDescription(  "disk mode" ).create( "vmDiskMode" ));

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
	
	
	public void execute(){
		
		VsphereServer vsphereServer=new VsphereServer(vsphereUrl, vsphereUsername,vspherePassword);
		try {
			vsphereServer.connect();
						
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
					vmCpuCount,vmGuestOsId,vmDiskSize,vmDiskMode,
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

			//vsphereServer.listNicsVm(newVm);
			if (vmOmapiRegister) {
				System.out.println("registering nic:"+vmPxeInterface);

				OmapiServer omapiServer=new OmapiServer(omapiHost,omapiPort,omapiKeyName, omapiKeyValue);
				String macAddress=vsphereServer.getMacAddress(vmPxeInterface,newVm);
				omapiServer.updateDHCP(vmName, macAddress,omapiOverWrite);
				System.out.println(macAddress);
			}

			vsphereServer.powerOnVm(newVm);		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void execute2() {
		System.out.println("executed");
		
//		config.vsphereUrl="https://192.168.2.152/sdk";
//		config.vspherePassword="pipopo";
//		config.vsphereUsername="root";
//		config.vmForce=true;
//		config.vmVncActivate=false;
//
//		config.vsphereDataCenterName = "ha-datacenter";
//		config.vsphereDataStoreName = "datastore1";
//
//		config.vmName = "app-oe-hardy2.mmis.be";   
//		config.vmMemorySize = 768;
//		config.vmCpuCount = 1;
//		config.vmGuestOsId = "ubuntuGuest";
//		//Is KB
//		config.vmDiskSize = 10000000;
//		config.vmDiskMode = "persistent";
//
//		config.vmInterfaces=new String[][]{
//				{"96", "Network Adapter 1"},
//				{"2096", "Network Adapter 2"},	    		
//				{"113", "Network Adapter 3"}
//		};
//
//		config.vmPxeInterface="Network Adapter 3";
//
//		config.omapiKeyValue="2YdVRKaJ4x41lDqHfA8rl8pHx95C4PmBgPcf5hIJ8j417HFN0AxUBEo6/3FoYyWjPyvXXCd+H6fPygtZd/iKxQ==";
//		config.omapiHost="192.168.2.150";
//		config.omapiPort="9991";
//		config.omapiKeyName="omapi_key";
//		
	}
	

	
	public void execute3() {
				
		ArgsConfig config=new ArgsConfig();
//		config.vsphereUrl="https://belgie.mmis.be/sdk";
//		config.vspherePassword="Nai0PreBidzI";
//		config.vsphereUsername="puppet-vsphere";
//
//		config.vmForce=true;
//		config.vmVncActivate=false;
//				
//		config.vsphereDataCenterName = "DC Ferrarris";
//		config.vsphereDataStoreName = "storc2vol1";
//
//		config.vmName = "wts-on-1.mmis.be";   
//		config.vmMemorySize = 768;
//		config.vmCpuCount = 1;
//		config.vmGuestOsId = "ubuntu64Guest";
//		//Is KB
//		config.vmDiskSize = 10000000;
//		config.vmDiskMode = "persistent";
//
//		config.vmInterfaces=new String[][]{
//				{"96", "Network Adapter 1"},
//				{"2096", "Network Adapter 2"},	    		
//				{"113", "Network Adapter 3"}
//		};
//
//		config.vmPxeInterface="Network Adapter 3";
		
		
		
//		config.omapiHost="192.168.113.4";
//		config.omapiPort=9991;
//		configomapiKeyName="omapi_key";
//		config.omapiKeyValue="2YdVRKaJ4x41lDqHfA8rl8pHx95C4PmBgPcf5hIJ8j417HFN0AxUBEo6/3FoYyWjPyvXXCd+H6fPygtZd/iKxQ==";

		//w5tV/kd0nBfVJMpdP9pZ7hPzHaGN0EZUN90eqkdfB390E9Bw+SaulZ/CDhhWz4UhCOAdlPjDsh/d0LDayDwmEw==

	}
	

}
