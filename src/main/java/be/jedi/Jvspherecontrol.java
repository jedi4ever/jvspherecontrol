/*================================================================================
Copyright (c) 2008 VMware, Inc. All Rights Reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

* Neither the name of VMware, Inc. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior 
written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.
================================================================================*/

package be.jedi;

import java.net.URL;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.talamonso.OMAPI.Connection;
import org.talamonso.OMAPI.Message;
import org.talamonso.OMAPI.Exceptions.OmapiConnectionException;
import org.talamonso.OMAPI.Exceptions.OmapiException;
import org.talamonso.OMAPI.Exceptions.OmapiInitException;
import org.talamonso.OMAPI.Exceptions.OmapiObjectException;
import org.talamonso.OMAPI.Objects.Host;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tightvnc.vncviewer.JvncKeySender;
import com.vmware.vim25.ConfigTarget;

import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.Description;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualFloppy;
import com.vmware.vim25.VirtualFloppyImageBackingInfo;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineBootOptions;
import com.vmware.vim25.VirtualMachineCapability;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigOption;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineDatastoreInfo;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineStorageInfo;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.EnvironmentBrowser;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class Jvspherecontrol {

	//Vsphere options
	boolean vsphereAction=false;
	String vsphereUrl, vsphereUsername, vspherePassword; 
	String vsphereDataCenterName, vsphereDataStoreName;
	
	boolean vmAction=false;
	boolean vmOverwrite=false;
	
	//Option GuesOsId - http://www.vmware.com/support/developer/windowstoolkit/wintk40u1/html/Set-VM.html
	// DiskMode - 	 mode: persistent|independent_persistent,independent_nonpersistent
	String vmName,  vmGuestOsId,vmDiskMode, vmCdromIsoFile;
	int  vmCpuCount;
	long vmMemorySize, vmDiskSize;
	String[] vmInterfaces[];
    // type: "generated", "manual", "assigned" by VC
	
	boolean vncActivate=false;
	String vncHost, vncPassword; 
	int vncPort; int vncWaitTime;
	
	boolean vncSendAction=false;
	String vncText[];

	String omapiHost;
	int omapiPort;
	String omapiKeyName;
	String omapiKeyValue;
	
	public static void main(String[] args) {
		Jvspherecontrol jvs=new Jvspherecontrol(args);
		try {
			jvs.doit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Jvspherecontrol(String[] args) {
		try {
			checkSsh();

			parseArguments(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void checkSsh() {
		
		JSch jcsh=new JSch();
		String sshUser="kapitein";
		String sshHost="192.168.2.150";
		String sshPassword="12345678";
		String sshKeyFile="";
		int sshTimeout=30000;
		int port=22;
		try {
			Session session=jcsh.getSession(sshUser, sshHost, port);
			session.setPassword(sshPassword);
			session.setConfig("StrictHostKeyChecking", "no");
			jcsh.addIdentity(sshKeyFile);
//			jcsh.addIdentity(sshKeyFile,sshKeyPassPhrase);
			session.connect(sshTimeout);
			
//			Channel channel=session.openChannel("exec");
//		    ((ChannelExec)channel).setCommand("who am i");
//		    channel.setInputStream(null);
//			channel.setOutputStream(System.out);
//			channel.connect();
//			channel.disconnect();
			session.disconnect();
			
			//com.jcraft.jsch.JSchException:
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			System.out.println("##"+e.getMessage());
			//Auth fail
			e.printStackTrace();
		}
			
	}
	
	
//	http://www.java-opensource.com/open-source/command-line-interpreters.html
	void parseArguments(String[]args2)throws Exception {
		
		String args[]= { "-vmNicName=curl","-vmNicName=\"vmnet3\"","-vsphereUrl=bla","-vmNicName=bla,vlan33"};

		Options options = new Options();
		Option help = new Option( "help", "print this message" ); options.addOption(help);
		options.addOption(OptionBuilder.withArgName( "url" ).hasArg().withDescription(  "url to connect to" ).create( "vsphereUrl" ));
		options.addOption(OptionBuilder.withArgName( "username" ).hasArg().withDescription(  "username to connect to vSphere" ).create( "vsphereUserName" ));
		options.addOption(OptionBuilder.withArgName( "password" ).hasArg().withDescription(  "password to connect to vSphere" ).create( "vspherePassword" ));
		options.addOption(OptionBuilder.withArgName( "datacentername" ).hasArg().withDescription(  "name of the datacenter to store new Vm" ).create( "vsphereDataCenterName" ));
		options.addOption(OptionBuilder.withArgName( "datastorename" ).hasArg().withDescription(  "name of the datastore to store new Vm" ).create( "vsphereDataStoreName" ));
		options.addOption(OptionBuilder.withArgName( "name" ).hasArg().withDescription(  "name of vm to create" ).create( "vmName" ));
		options.addOption(OptionBuilder.withArgName( "guestOsId" ).hasArg().withDescription(  "type of vm to create" ).create( "vmGuestOsId" ));

		options.addOption(OptionBuilder.withArgName( "disksize" ).hasArg().withDescription(  "size in kb of disk to create" ).create( "vmDiskSize" ));
		options.addOption(OptionBuilder.withArgName( "cpucount" ).hasArg().withDescription(  "number of cpu's to allocate" ).create( "vmCpuCount" ));
		options.addOption(OptionBuilder.withArgName( "size in MB" ).hasArg().withDescription(  "memory size to allocate" ).create( "vmMemorySize" ));

		options.addOption(OptionBuilder.withArgName( "filename" ).hasArg().withDescription(  "dvd isofile" ).create( "vmCdromIsoFile" ));

		///Option GuesOsId - http://www.vmware.com/support/developer/windowstoolkit/wintk40u1/html/Set-VM.html
		
		options.addOption(OptionBuilder.withArgName( "persistent|independent_persistent|independent_nonpersistent" ).hasArg().withDescription(  "disk mode" ).create( "vmDiskMode" ));

		
		options.addOption(OptionBuilder.withArgName( "nickname" ).hasArgs().withDescription(  "name of the network interface" ).create( "vmNicName" ));
		options.addOption(OptionBuilder.withArgName( "nicktype" ).hasArgs().withDescription(  "name of the network interface" ).create( "vmNicType" ));
		
		options.addOption(OptionBuilder.withArgName( "text" ).hasArgs().withDescription(  "text to send" ).create( "vncText" ));

		Option vmNameOption = new Option( "vmOverwrite", "the name of the vm to create" ); options.addOption(vmNameOption);

		Option vmActionOption = new Option( "vmAction", "create a new vm" ); options.addOption(vmActionOption);
		Option vmOverwriteOption = new Option( "vmOverwrite", "overwrite existing vm with same name" ); options.addOption(vmOverwriteOption);
		
		
		// create the parser
	    CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        String[] nicNames=line.getOptionValues("vmNicName");
	        System.out.println(nicNames.length);

	        System.out.println(nicNames[0]+"-"+ nicNames[1]);
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
	    
	 // automatically generate the help statement
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp( "java -jar jvspherecontrol.jar ", options );
	    
	    System.exit(0);
	}
	
//	http://webcache.googleusercontent.com/search?q=cache:ds5QNEAJ_coJ:download3.vmware.com/sample_code/Java/CreateVMStaticMACAddress.html+get+mac+address+after+create+vm+java&cd=7&hl=nl&ct=clnk&gl=be&client=safari		

// Mac generator = http://communities.vmware.com/thread/25126
	
  void parseArguments3(String[] args)  throws Exception {
	  
	  vsphereAction=true;
	  vsphereUrl= "https://192.168.2.152/sdk";
	    vsphereUsername="root";
	    vspherePassword="pipopo";
	    vsphereDataCenterName = "ha-datacenter";
	    vsphereDataStoreName = "datastore1";

	    vmName = "app-oe-laurel2";   
	    vmMemorySize = 1024;
	    vmCpuCount = 2;
	    vmGuestOsId = "ubuntuGuest";
	    //Is KB
	    vmDiskSize = 10000000;
	    vmDiskMode = "persistent";
	    String vmCdromDataStoreName=vsphereDataStoreName;
	    vmCdromIsoFile =  "[" + vmCdromDataStoreName +"] "+ vmName + "/" +"ubuntu.iso";
   
	    vmInterfaces=new String[][]{
	    		{"VM Network", "Network Adapter 1"},
	    		{"VLAN-1201", "Network Adapter 2"}	    		
	    };
	    
		vncActivate=true;
	    vncHost="192.168.2.152";
	    vncPassword="secret";
	    vncPort=5901;

	    vncWaitTime=10;
	    vncSendAction=true;
	    
//		jvnc.print("/install/vmlinuz noapic netcfg/get_hostname=jeos netcfg/choose_interface=eth0 netcfg/wireless_wep= preseed/url=http://192.168.2.30/<TILDE>patrick/preseed.cfg debian-installer=en_US auto locale=en_US kbd-chooser/method=us ");
//		jvnc.print("/install/vmlinuz noapic preseed/file=/floppy/preseed.cfg debian-installer=en_US auto locale=en_US kbd-chooser/method=us ");
	
	    vncText=new String[]{ "<ESC><ESC><RETURN>",
	    		"/install/vmlinuz noapic" +
	    				" hostname=pxeboot domain=jedi2.be netcfg/get_domain=jedi.be interface=eth0 netcfg/get_ipaddress=192.168.2.150 netcfg/get_netmask=255.255.255.0 netcfg/get_gateway=192.168.2.10 " ,
	    				" netcfg/get_nameservers=192.168.2.10 netcfg/disable_dhcp=true" ,
	    				" netcfg/choose_interface=eth0 netcfg/wireless_wep= preseed/url=http://192.168.2.30/<TILDE>patrick/preseed.cfg debian-installer=en_US auto locale=en_US kbd-chooser/method=us",
	    				" fb=false debconf/frontend=noninteractive", 
	    				" console-setup/ask_detect=false console-setup/modelcode=pc105 console-setup/layoutcode=us ",
	    				" initrd=/install/initrd.gz -- <RETURN>"
	    }; 

  }
  
  void parseArguments2(String[] args)  throws Exception {
	  
	  vsphereUrl= "https://192.168.2.152/sdk";
	    vsphereUsername="root";
	    vspherePassword="pipopo";
	    vsphereDataCenterName = "ha-datacenter";
	    vsphereDataStoreName = "datastore1";

	    vmName = "app-oe-hardy2";   
	    vmMemorySize = 768;
	    vmCpuCount = 1;
	    vmGuestOsId = "ubuntuGuest";
	    //Is KB
	    vmDiskSize = 10000000;
	    vmDiskMode = "persistent";
   
	    vmInterfaces=new String[][]{
	    		{"VM Network", "Network Adapter 1"},
	    		{"VLAN-1201", "Network Adapter 2"}	    		
	    };

		omapiHost="192.168.2.150";
		omapiPort=9991;
		omapiKeyName="omapi_key";
		omapiKeyValue="2YdVRKaJ4x41lDqHfA8rl8pHx95C4PmBgPcf5hIJ8j417HFN0AxUBEo6/3FoYyWjPyvXXCd+H6fPygtZd/iKxQ==";
		
  }
  
  void updateDHCP(String name, String macAddress) throws OmapiInitException, OmapiConnectionException {
		Connection c = null;
		try {
			c = new Connection(omapiHost, omapiPort);
			c.setAuth(omapiKeyName, omapiKeyValue);
		} catch (OmapiException e) {
			System.err.println(e.getMessage());
		}

		try {

			Host h3 = new Host(c);
			h3.setHardwareAddress(macAddress);
			// h.setName("albert");
			Host remote3 = h3.send(Message.OPEN);
			
			Host h = new Host(c);
			h.updateIPAddress("1.3.1.46");
			h.setName("albert2");
			Host remote = h.send(Message.UPDATE);
			
			Host h2 = new Host(c);
			h2.setName("albert");
			h2.setIPAddress("1.2.3.4");
			h2.setHardwareAddress(macAddress);
			h2.setHardwareType(1);
			Host remote2 = h2.send(Message.CREATE);
			
			
		} catch (OmapiObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  
  }
  
  public  void doit() throws Exception 
  {  
 
    
    ServiceInstance si = new ServiceInstance(
        new URL(vsphereUrl), vsphereUsername, vspherePassword, true);

    Folder rootFolder = si.getRootFolder();
    
    ManagedEntity[] hosts= new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem"); 


    for(int i=0; i<hosts.length; i++)
    {
    	
    	HostSystem host= (HostSystem) hosts[i];
    	System.out.println("host:"+host.getName());

    }
    
    System.out.println("available datacenters:");
    ManagedEntity[] datacenters = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");

    for(int i=0; i<datacenters.length; i++)
    {

    	Datacenter dc = (Datacenter) datacenters[i];
    	System.out.println("Datacenters:"+dc.getName());
    	
    	Datastore[] stores=dc.getDatastores();
        for(int d=0; d<stores.length; d++) {
        	System.out.println("Store:"+stores[d].getName());
        }	
       
        Network[] nets=dc.getNetworks();
        for(int d=0; d<nets.length; d++) {
        	System.out.println("Nets:"+nets[d].getName());
        }	
        
        
    }
   
    //Find if it already exists!
	VirtualMachine existingVm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine",vmName);
	if (existingVm!=null) {
		System.out.println("Machine exists with name:");
		System.out.println(existingVm.getName());
		
		VirtualMachineConfigInfo vminfo = existingVm.getConfig();
		VirtualMachineCapability vmc = existingVm.getCapability();

		VirtualHardware hw= vminfo.getHardware();
		 VirtualDevice [] devices = hw.getDevice();
		 for(int i=0;i<devices.length;i++)
		 	{
			 System.out.println(devices[i].getDeviceInfo().getLabel());
			 System.out.println(devices[i].getDeviceInfo().getSummary());

			 if (devices[i] instanceof VirtualEthernetCard) {
					
				 System.out.println("network device");

				 if (devices[i] instanceof VirtualE1000) {
						
					 System.out.println("found E1000");

				 }
				 
			 }
			 System.out.println(devices[i].getUnitNumber());
		 	} 
		
		
		//Poweroff if poweron
		 System.out.println((existingVm.getName()));
		  	VirtualMachineSummary summary = (VirtualMachineSummary) (existingVm.getSummary());
		  	VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) existingVm.getRuntime();
		  	if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn)
		  	{
		  		Task task = existingVm.powerOffVM_Task();
		  		task.waitForTask();
		 	System.out.println("vm:" + existingVm.getName() + " powered off.");
			} 

		  	
		  	
		//	 System.exit(5);
		 	System.out.println("vm:" + existingVm.getName() + " removed");

			//Other strategy is to suspend it and rename the machine (so we still have it)
		  	Task destroytask=existingVm.destroy_Task();
			destroytask.waitForTask();

//		  	destroytask.waitForMe();
	
	} else {
		
	}

	
    
//	ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

    Datacenter dc = (Datacenter) new InventoryNavigator(
       rootFolder).searchManagedEntity("Datacenter", vsphereDataCenterName);
 //    ResourcePool rp = (ResourcePool) new InventoryNavigator(
 //    dc).searchManagedEntities("ResourcePool")[0];
  ResourcePool rp = (ResourcePool) new InventoryNavigator(
        dc).searchManagedEntities("ResourcePool")[0];
      
    Folder vmFolder = dc.getVmFolder();

    // create vm config spec
    VirtualMachineConfigSpec vmSpec = 
      new VirtualMachineConfigSpec();
    vmSpec.setName(vmName);
    vmSpec.setAnnotation("VirtualMachine Annotation");
    vmSpec.setMemoryMB(vmMemorySize);
    vmSpec.setNumCPUs(vmCpuCount);
    vmSpec.setGuestId(vmGuestOsId);

    // create virtual devices
    int cKey = 1000;
    VirtualDeviceConfigSpec scsiSpec = createScsiSpec(cKey);
    VirtualDeviceConfigSpec diskSpec = createDiskSpec(
    		vsphereDataStoreName, cKey, vmDiskSize, vmDiskMode);
    
    VirtualDeviceConfigSpec machineSpecs[]= new VirtualDeviceConfigSpec[vmInterfaces.length+2];
    machineSpecs[0]=scsiSpec;
    machineSpecs[1]=diskSpec;
    
    //TODO: Loop
    for (int i=0; i< vmInterfaces.length; i++ ) {
    	   
    	machineSpecs[2+i]= createNicSpec(
    		        vmInterfaces[i][0], vmInterfaces[i][1]);
    }   
    
    vmSpec.setDeviceChange(machineSpecs);
    
    // create vm file info for the vmx file
    VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
    vmfi.setVmPathName("["+ vsphereDataStoreName +"]");
    vmSpec.setFiles(vmfi);

    // call the createVM_Task method on the vm folder
    // TOO: null, host ?
    Task task = vmFolder.createVM_Task(vmSpec, rp, null);
    String result= task.waitForTask();
//    String result = task.waitForMe();   

    
    VirtualMachine newVm=null;
    
    if(result == Task.SUCCESS) 
    {
      System.out.println("VM Created Sucessfully");
      
      //Search for it and start it if requested
      //TODO powerOnVM_Task (null) =!! null = host , maar was is die host?
      
  		 newVm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine",vmName);
      } else 
    {
      System.out.println("VM could not be created. ");
    }
    
    
    
   //No add the cdrom
   
    if (vmCdromIsoFile!=null) {
    	   VirtualDeviceConfigSpec cdSpec = createAddCdConfigSpec(newVm, vsphereDataStoreName, vmCdromIsoFile);
    	    VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
    	    

    	    vmConfigSpec.setDeviceChange(new VirtualDeviceConfigSpec[]{cdSpec});

    	    Task cdtask = newVm.reconfigVM_Task(vmConfigSpec);
    	    String cdresult=cdtask.waitForTask();

    	    if(cdresult == Task.SUCCESS) 
    	    {
    	      System.out.println("Cdrom Created Sucessfully");
    	 
    	    } else {
    	        System.out.println("Cdrom NOT Created Sucessfully Error:"+cdresult);
    	    	
    	    }
   	
    }
     
    //TODO : Add floppy!
    
    // Enter bios for firs boot -> http://blogs.microsoft.co.il/blogs/scriptfanatic/archive/2009/08/27/force-a-vm-to-enter-bios-setup-screen-on-next-reboot.aspx
    
    VirtualMachineBootOptions bootoptions= new VirtualMachineBootOptions();
    bootoptions.setEnterBIOSSetup(true);
 
    //Net boot order
    //bios.bootDeviceClasses
    //allow:cd,hd,net
    //http://download3.vmware.com/sample_code/Perl/VMBootOrder.html
    
    
    //Hardening
//    isolation.device.connectable.disable = "true"
//    	isolation.device.edit.disable = "true"
//    	isolation.tools.setOption.disable = "true"
//    	isolation.tools.log.disable = "true"
//    	isolation.tools.diskWiper.disable = "true"
//    	isolation.tools.diskShrink.disable = "true"
//    	isolation.tools.copy.disable = "true"
//    	isolation.tools.paste.disable = "true"
//    	isolation.tools.setGUIOptions.enable = "false"
//    	log.rotateSize = "100000"
//    	log.keepOld = "10"
//    	vlance.noOprom = "true"
//    	vmxnet.noOprom = "true"

    
    //Set only boot from net on one of the interface (disable pxe)
    //http://virtualfoundry.blogspot.com/2009/05/secrets-of-e1000.html
    //http://kb.vmware.com/selfservice/microsites/search.do?language=en_US&cmd=displayKC&externalId=1014906
    //    ethernet0.opromsize = "0"
    //vlance.noOprom = "true" or vmxnet.noOprom = "true"

    
	if (newVm!=null) {
		Task powerOnTask=newVm.powerOnVM_Task(null);
		powerOnTask.waitForTask();
	      System.out.println("Poweringon");

	}

	if (newVm!=null) {
		Task powerOffTask=newVm.powerOffVM_Task();
		powerOffTask.waitForTask();
	      System.out.println("Powered down");
	}
	
	//Read the macaddress generated by Vmware
	VirtualMachineConfigInfo vminfo = newVm.getConfig();
	VirtualMachineCapability vmc = newVm.getCapability();

	VirtualHardware hw= vminfo.getHardware();
	 VirtualDevice [] devices = hw.getDevice();
	 for(int i=0;i<devices.length;i++)
	 	{
	
		 if (devices[i] instanceof VirtualEthernetCard) {

			 VirtualEthernetCard newnic=null;
			 newnic=(VirtualEthernetCard) devices[i];
			 System.out.println(newnic.getMacAddress());
			 System.out.println("network device");
			 System.out.println(devices[i].getDeviceInfo().getLabel());
			 System.out.println(devices[i].getDeviceInfo().getSummary());

			 if (devices[i] instanceof VirtualE1000) {
					
				 System.out.println("found E1000");

			 }
			 
		 }
	 	} 
	
	 
	 if (vncActivate) {
		 //Extraconfig setting : 
			// http://blogs.vmware.com/vipowershell/2008/09/changing-vmx-fi.html
			// Extraconfig gebruik : http://sourceforge.net/projects/vijava/forums/forum/826592/topic/3756870?message=8491628
//			 RemoteDisplay.vnc.enabled = "TRUE"
//			 RemoteDisplay.vnc.password = "your_password"
//			 RemoteDisplay.vnc.port = "5900"
			 
			 OptionValue vnc1=new OptionValue() ;
			 vnc1.setKey("RemoteDisplay.vnc.enabled");
			 vnc1.setValue("TRUE");

			 OptionValue vnc2=new OptionValue() ;
			 vnc2.setKey("RemoteDisplay.vnc.password");
			 vnc2.setValue(vncPassword);

			 OptionValue vnc3=new OptionValue() ;
			 vnc3.setKey("RemoteDisplay.vnc.port");
			 vnc3.setValue(String.valueOf(vncPort));
			 
			 
			 // Default VNC is not enabled in ESX 
			 // Option 1-> use the ssh tunnel 
			 // Option 2-> start the vnc stuff 
			 //http://www.novell.com/communities/node/6544/launch-remote-console-vms-esx-server-platespin-orchestrator-without-installing-vnc-server-
			 //esxcfg-firewall -e vncServer
			 //Currently VMware supports 5901 - 5964 ports. It means at a time we can launch remote consoles for 65 VMs.
			 
			 OptionValue[] extraConfig= { vnc1, vnc2, vnc3 };
			 
			 //newVm.getConfig().setExtraConfig(extraConfig);
			 
			    VirtualMachineConfigSpec vmConfigSpec2 = new VirtualMachineConfigSpec();
			    vmConfigSpec2.setExtraConfig(extraConfig);
			    
			    Task vnctask = newVm.reconfigVM_Task(vmConfigSpec2);
			    String vncresult=vnctask.waitForTask();

			    if(vncresult == Task.SUCCESS) 
			    {
			      System.out.println("VNC Set Sucessfully");
			 
			    } else {
			        System.out.println("VNC Set Error:"+vncresult);
			    	
			    }		 
	 }

	
		if (newVm!=null) {
			Task powerOnTask=newVm.powerOnVM_Task(null);
			powerOnTask.waitForTask();
		      System.out.println("Powering for real");

		}

		if (vncSendAction) {
			sleep(vncWaitTime);
			
			// Ignore Cert file
			// https://www.chemaxon.com/forum/ftopic65.html&highlight=jmsketch+signer
			//preseed.jeos is next? use vnc ???
			
			JvncKeySender jvnc=new JvncKeySender(vncHost,vncPort,vncPassword);
			try {
				jvnc.open();


				for (int i=0; i< vncText.length; i++) {
					jvnc.print(vncText[i]);
					sleep(1);
				}

				jvnc.close();

			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(ex.toString());
			}

		}

		
		
  }

  static VirtualDeviceConfigSpec createScsiSpec(int cKey)
  {
    VirtualDeviceConfigSpec scsiSpec = 
      new VirtualDeviceConfigSpec();
    scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
    VirtualLsiLogicController scsiCtrl = 
        new VirtualLsiLogicController();
    scsiCtrl.setKey(cKey);
    scsiCtrl.setBusNumber(0);
    scsiCtrl.setSharedBus(VirtualSCSISharing.noSharing);
    scsiSpec.setDevice(scsiCtrl);
    return scsiSpec;
  }
 
	static void sleep(int seconds) {
		//We need to wait
		try{
				  Thread.currentThread().sleep(seconds*1000);//sleep for 1000 ms
			}
			catch(InterruptedException ie){
		}
	}
	
  static VirtualDeviceConfigSpec createDiskSpec(String dsName, 
      int cKey, long diskSizeKB, String diskMode)
  {
    VirtualDeviceConfigSpec diskSpec = 
        new VirtualDeviceConfigSpec();
    diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
    diskSpec.setFileOperation(
        VirtualDeviceConfigSpecFileOperation.create);
    
    VirtualDisk vd = new VirtualDisk();
    vd.setCapacityInKB(diskSizeKB);
    diskSpec.setDevice(vd);
    vd.setKey(0);
    vd.setUnitNumber(0);
    vd.setControllerKey(cKey);

    VirtualDiskFlatVer2BackingInfo diskfileBacking = 
        new VirtualDiskFlatVer2BackingInfo();
    String fileName = "["+ dsName +"]";
    diskfileBacking.setFileName(fileName);
    diskfileBacking.setDiskMode(diskMode);
    diskfileBacking.setThinProvisioned(true);
    vd.setBacking(diskfileBacking);
    return diskSpec;
  }
  
  
  
  static VirtualDeviceConfigSpec createFloppySpec(VirtualMachine vm, String dsName,  String floppyName) throws Exception {

	    VirtualDeviceConfigSpec floppySpec =  new VirtualDeviceConfigSpec();
	    floppySpec.setOperation(VirtualDeviceConfigSpecOperation.add);
	    
	    VirtualFloppy floppy= new VirtualFloppy();
	 
	    
	    VirtualFloppyImageBackingInfo floppyBacking = new  VirtualFloppyImageBackingInfo();
	    DatastoreSummary ds = findDatastoreSummary(vm, dsName);

	    floppyBacking.setDatastore(ds.getDatastore());
	    floppyBacking.setFileName("[" + dsName +"] "+ vm.getName() 
		        + "/" + floppyName);
	    
	    floppy.setBacking(floppyBacking);
	    

//	    VirtualDevice vd = getIDEController(vm);          
//	    cdrom.setBacking(cdDeviceBacking);                    
//	    cdrom.setControllerKey(vd.getKey());
//	    cdrom.setUnitNumber(vd.getUnitNumber());
//	    cdrom.setKey(-1);          

	    floppySpec.setDevice(floppy);
	    
	    return floppySpec;
	             
  }
  
//  http://webcache.googleusercontent.com/search?q=cache:-IaJ930Lu4oJ:communities.vmware.com/servlet/JiveServlet/download/10742-1-28258/VMNetworkingOps.java%3Bjsessionid%3D08B9FD441B37D6093CEFFBF35C7C0909+VirtualPCNet32+java+E1000&cd=1&hl=nl&ct=clnk&gl=be&client=firefox-a 
// nic types = e1000,pcnet32,vmxnet2,vmxnet3
  static VirtualDeviceConfigSpec createNicSpec(String netName, 
      String nicName) throws Exception
  {
    VirtualDeviceConfigSpec nicSpec = 
        new VirtualDeviceConfigSpec();
    nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

    
    VirtualEthernetCard nic =  new VirtualE1000();
    //Create defalut E1000 adapter
//    VirtualEthernetCard vd = new VirtualE1000();
//    
//    if (adapterType.equalsIgnoreCase(PCNET32)) {
//        vd = new VirtualPCNet32();
//    } else if (adapterType.equalsIgnoreCase(VMXNET2)) {
//        vd = new VirtualVmxnet2();
//    } else if (adapterType.equalsIgnoreCase(VMXNET3)) {
//        vd = new VirtualVmxnet3();
//    }

    VirtualEthernetCardNetworkBackingInfo nicBacking = 
        new VirtualEthernetCardNetworkBackingInfo();
    nicBacking.setDeviceName(netName);

    Description info = new Description();
    info.setLabel(nicName);
    info.setSummary(netName);
    nic.setDeviceInfo(info);
    
    // type: "generated", "manual", "assigned" by VC
    nic.setAddressType("generated");
    nic.setBacking(nicBacking);
    nic.setKey(0);
   
    nicSpec.setDevice(nic);
    return nicSpec;
  }
  
  static VirtualDeviceConfigSpec createAddCdConfigSpec(VirtualMachine vm, String dsName, String isoName) throws Exception 
  {
    VirtualDeviceConfigSpec cdSpec = new VirtualDeviceConfigSpec();

    cdSpec.setOperation(VirtualDeviceConfigSpecOperation.add);         

    VirtualCdrom cdrom =  new VirtualCdrom();
    VirtualCdromIsoBackingInfo cdDeviceBacking = new  VirtualCdromIsoBackingInfo();
    DatastoreSummary ds = findDatastoreSummary(vm, dsName);
    cdDeviceBacking.setDatastore(ds.getDatastore());
    cdDeviceBacking.setFileName( isoName);
//    cdDeviceBacking.setFileName("[" + dsName +"] "+ vm.getName() 
//            + "/" + isoName);

    VirtualDevice vd = getIDEController(vm);          
    cdrom.setBacking(cdDeviceBacking);                    
    cdrom.setControllerKey(vd.getKey());
    cdrom.setUnitNumber(vd.getUnitNumber());
    cdrom.setKey(-1);          

    cdSpec.setDevice(cdrom);

    return cdSpec;          
  }
  
  static VirtualDeviceConfigSpec createRemoveCdConfigSpec(VirtualMachine vm, String cdName) throws Exception 
  {
    VirtualDeviceConfigSpec cdSpec = new VirtualDeviceConfigSpec();
    cdSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);
    VirtualCdrom cdRemove = (VirtualCdrom)findVirtualDevice(vm.getConfig(), cdName);
    if(cdRemove != null) 
    {
      cdSpec.setDevice(cdRemove);
      return cdSpec;
    }
    else 
    {
      System.out.println("No device available " + cdName);
      return null;
    }
  }

  private static VirtualDevice findVirtualDevice(
      VirtualMachineConfigInfo vmConfig, String name)
  {
    VirtualDevice [] devices = vmConfig.getHardware().getDevice();
    for(int i=0;i<devices.length;i++)
    {
      if(devices[i].getDeviceInfo().getLabel().equals(name))
      {                             
        return devices[i];
      }
    }
    return null;
  }

  static DatastoreSummary findDatastoreSummary(VirtualMachine vm, String dsName) throws Exception 
  {
    DatastoreSummary dsSum = null;
    VirtualMachineRuntimeInfo vmRuntimeInfo = vm.getRuntime();
    EnvironmentBrowser envBrowser = vm.getEnvironmentBrowser(); 
    ManagedObjectReference hmor = vmRuntimeInfo.getHost();

    if(hmor == null)
    {
      System.out.println("No Datastore found");
      return null;
    }
    
    ConfigTarget configTarget = envBrowser.queryConfigTarget(new HostSystem(vm.getServerConnection(), hmor));
    VirtualMachineDatastoreInfo[] dis = configTarget.getDatastore();
    for (int i=0; dis!=null && i<dis.length; i++) 
    {
      dsSum = dis[i].getDatastore();
      if (dsSum.isAccessible() && dsName.equals(dsSum.getName())) 
      {
        break;
      }
    }
    return dsSum;
  }

  static VirtualDevice getIDEController(VirtualMachine vm) 
    throws Exception 
  {
    VirtualDevice ideController = null;
    VirtualDevice [] defaultDevices = getDefaultDevices(vm);
    for (int i = 0; i < defaultDevices.length; i++) 
    {
      if (defaultDevices[i] instanceof VirtualIDEController) 
      {
        ideController = defaultDevices[i];             
        break;
      }
    }
    System.out.println(ideController.toString());
    return ideController;
  }

  static VirtualDevice[] getDefaultDevices(VirtualMachine vm) 
  throws Exception 
  {
    VirtualMachineRuntimeInfo vmRuntimeInfo = vm.getRuntime();
    EnvironmentBrowser envBrowser = vm.getEnvironmentBrowser(); 
    ManagedObjectReference hmor = vmRuntimeInfo.getHost();
    VirtualMachineConfigOption cfgOpt = envBrowser.queryConfigOption(null, new HostSystem(vm.getServerConnection(), hmor));
    VirtualDevice[] defaultDevs = null;
    if (cfgOpt != null) 
    {
      defaultDevs = cfgOpt.getDefaultDevice();
      if (defaultDevs == null) 
      {
        throw new Exception("No Datastore found in ComputeResource");
      }
    }
    else
    {
      throw new Exception("No VirtualHardwareInfo found in ComputeResource");
    }
    return defaultDevs;
  }

  
}