package be.jedi.jvspherecontrol.vsphere;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.vmware.vim25.ConcurrentAccess;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidDatastore;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.UserSearchResult;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineBootOptions;
import com.vmware.vim25.VirtualMachineCapability;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import be.jedi.jvspherecontrol.VmDisk;
import be.jedi.jvspherecontrol.VmNic;
import be.jedi.jvspherecontrol.vsphere.VsphereUtils;

public class VsphereServer {

	String vsphereUrl, vsphereUsername,vspherePassword;

	Folder rootFolder;
	ServiceInstance si;

	public VsphereServer(String vsphereUrl, String vsphereUsername,
			String vspherePassword) {
		super();
		this.vsphereUrl = vsphereUrl;
		this.vsphereUsername = vsphereUsername;
		this.vspherePassword = vspherePassword;
	}



	public void connect() throws RemoteException, MalformedURLException {

		si = new ServiceInstance(
				new URL(vsphereUrl), vsphereUsername, vspherePassword, true);
		
	
		rootFolder = si.getRootFolder();

	}

	public ArrayList<String> listHosts() throws InvalidProperty, RuntimeFault, RemoteException {
		ManagedEntity[] hosts= new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem"); 

		ArrayList<String> hostList=new ArrayList<String>();
		
		for(int i=0; i<hosts.length; i++)
		{

			HostSystem host= (HostSystem) hosts[i];
			hostList.add(host.getName());

		}
		
		return hostList;
	}


	//http://www.vmware.com/support/developer/vc-sdk/visdk41pubs/ApiReference/vim.UserDirectory.html

	public ArrayList<String> listUsers() {
		ArrayList<String> userList=new ArrayList<String>();		
		
		try {
			UserSearchResult[] users=si.getUserDirectory().retrieveUserGroups(null, "", null, null, false, true, true);
			for (int u=0; u<users.length; u++) {
				userList.add(users[u].getPrincipal());
			}
		} catch (NotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return userList;
		
	}
	
	public ArrayList<String> listDataCenters() throws InvalidProperty, RuntimeFault, RemoteException {

		ManagedEntity[] datacenters = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");

		ArrayList<String> datacenterList=new ArrayList<String>();
		
		for(int i=0; i<datacenters.length; i++)
		{
			Datacenter dc = (Datacenter) datacenters[i];
			datacenterList.add(dc.getName());
		}
		return datacenterList;
	}

	public ArrayList<String> listDataStores() throws InvalidProperty, RuntimeFault, RemoteException {

		ManagedEntity[] datacenters = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");

		ArrayList<String> datastoreList=new ArrayList<String>();
		
		for(int i=0; i<datacenters.length; i++)
		{
			Datacenter dc = (Datacenter) datacenters[i];
			Datastore[] stores=dc.getDatastores();
			for(int d=0; d<stores.length; d++) {
				datastoreList.add(stores[d].getName());
			}	
		}
		
		return datastoreList;

	}

	public ArrayList<String> listNetworks() throws InvalidProperty, RuntimeFault, RemoteException {

		ManagedEntity[] datacenters = 
			new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
		
		ArrayList<String> networkList=new ArrayList<String>();

		for(int i=0; i<datacenters.length; i++)
		{

			Datacenter dc = (Datacenter) datacenters[i];

			Network[] nets=dc.getNetworks();
			for(int d=0; d<nets.length; d++) {
				networkList.add(nets[d].getName());
			}
		}

		return networkList;
	}

	public ArrayList<String> listVms() throws InvalidProperty, RuntimeFault, RemoteException  {

		ArrayList<String> vmList=new ArrayList<String>();
		
		ManagedEntity[] vms=new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

		for(int i=0; i<vms.length; i++)
		{
			vmList.add(vms[i].getName());
		}
		return vmList;
	}
	
	
	public VirtualMachine findVmByName(String vmName) throws InvalidProperty, RuntimeFault, RemoteException {
		VirtualMachine existingVm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine",vmName);

		return existingVm;
	}

	public void showVmDetail(VirtualMachine vm) {
		System.out.println(vm.getName());

		VirtualMachineConfigInfo vminfo = vm.getConfig();
		VirtualMachineCapability vmc = vm.getCapability();

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
	}



	public void powerOffVm(VirtualMachine existingVm) throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {
		Task task = existingVm.powerOffVM_Task(); 		
		task.waitForTask();
		System.out.println("vm:" + existingVm.getName() + " powered off.");		
	}



	public void destroyVm(VirtualMachine existingVm) throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException {

		String vmName=existingVm.getName();

		//Other strategy is to suspend it and rename the machine (so we still have it)
		Task destroytask=existingVm.destroy_Task();
		destroytask.waitForTask();
		System.out.println("vm:" + vmName + " is destroyed");

	}

	public boolean isVmPoweredOn(VirtualMachine vm) {
		VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
		return (vmri.getPowerState() == VirtualMachinePowerState.poweredOn);

	}

	public void powerOnVm(VirtualMachine newVm) throws VmConfigFault, TaskInProgress, FileFault, InvalidState, InsufficientResourcesFault, RuntimeFault, RemoteException, InterruptedException {
		String vmName=newVm.getName();

		Task powerOnTask=newVm.powerOnVM_Task(null);
		powerOnTask.waitForTask();
		System.out.println("vm"+vmName+" is powered on");

	}


	public void vncDeActivateVm(VirtualMachine newVm) throws InvalidName, VmConfigFault, DuplicateName, TaskInProgress, FileFault, InvalidState, ConcurrentAccess, InvalidDatastore, InsufficientResourcesFault, RuntimeFault, RemoteException, InterruptedException {
		OptionValue currentExtraConfig[]=newVm.getConfig().getExtraConfig();

		//Remove extra config
		// http://communities.vmware.com/message/1229409
		
		OptionValue newExtraConfig[];
		
		//determine the number of RemoteDisplay flags
		int counter=0;
		
		
		for (int i=0; i <currentExtraConfig.length;  i++)
		{
			if (currentExtraConfig[i].getKey().startsWith("RemoteDisplay.vnc")) {
				counter++;
			}
//			System.out.println(currentExtraConfig[i].getKey());
		}

		newExtraConfig=new OptionValue[currentExtraConfig.length-counter];

		int newcounter=0;
		for (int i=0; i <currentExtraConfig.length;  i++)
		{
			if (!currentExtraConfig[i].getKey().startsWith("RemoteDisplay.vnc")) {
				newExtraConfig[newcounter]=currentExtraConfig[i];
//				System.out.println(newExtraConfig[newcounter].getKey());

				newcounter++;
			}
		}


		OptionValue vnc1=new OptionValue() ;vnc1.setKey("RemoteDisplay.vnc.enabled");vnc1.setValue("FALSE");
		OptionValue vnc2=new OptionValue() ;vnc2.setKey("RemoteDisplay.vnc.password");vnc2.setValue("");
		OptionValue vnc3=new OptionValue() ;vnc3.setKey("RemoteDisplay.vnc.port");vnc3.setValue("0");
		OptionValue vnc4=new OptionValue() ;vnc4.setKey("RemoteDisplay.vnc.key");vnc4.setValue("");

		OptionValue[] extraConfig= { vnc1, vnc2, vnc3, vnc4 };

		VirtualMachineConfigSpec vmConfigSpec2 = new VirtualMachineConfigSpec();
		vmConfigSpec2.setExtraConfig(extraConfig);

		Task vnctask = 	newVm.reconfigVM_Task(vmConfigSpec2);
		String vncresult=  	vnctask.waitForTask();

		if(vncresult == Task.SUCCESS) 
		{
			System.out.println("VNC deactivated Sucessfully");

		} else {
			System.out.println("VNC deactivate Error:"+vncresult);

		}		 

	}
	

	public void vncActivateVm(VirtualMachine newVm, int vncPort, String vncPassword) throws InvalidName, VmConfigFault, DuplicateName, TaskInProgress, FileFault, InvalidState, ConcurrentAccess, InvalidDatastore, InsufficientResourcesFault, RuntimeFault, RemoteException, InterruptedException {
		//Extraconfig setting : 
		// http://blogs.vmware.com/vipowershell/2008/09/changing-vmx-fi.html
		// Extraconfig gebruik : http://sourceforge.net/projects/vijava/forums/forum/826592/topic/3756870?message=8491628
		//		 RemoteDisplay.vnc.enabled = "TRUE"
		//		 RemoteDisplay.vnc.password = "your_password"
		//		 RemoteDisplay.vnc.port = "5900"

		OptionValue vnc1=new OptionValue() ;vnc1.setKey("RemoteDisplay.vnc.enabled");vnc1.setValue("TRUE");
		OptionValue vnc2=new OptionValue() ;vnc2.setKey("RemoteDisplay.vnc.password");vnc2.setValue(vncPassword);
		OptionValue vnc3=new OptionValue() ;vnc3.setKey("RemoteDisplay.vnc.port");vnc3.setValue(String.valueOf(vncPort));
		OptionValue[] extraConfig= { vnc1, vnc2, vnc3 };

		// Need to get the old extraconfig + add new stuff
		
		// Default VNC is not enabled in ESX 
		// Option 1-> use the ssh tunnel 
		// Option 2-> start the vnc stuff 
		//http://www.novell.com/communities/node/6544/launch-remote-console-vms-esx-server-platespin-orchestrator-without-installing-vnc-server-
		//esxcfg-firewall -e vncServer
		//Currently VMware supports 5901 - 5964 ports. It means at a time we can launch remote consoles for 65 VMs.

		VirtualMachineConfigSpec vmConfigSpec2 = new VirtualMachineConfigSpec();
		vmConfigSpec2.setExtraConfig(extraConfig);
	

		Task vnctask = 	newVm.reconfigVM_Task(vmConfigSpec2);
		String vncresult=  	vnctask.waitForTask();

		if(vncresult == Task.SUCCESS) 
		{
			System.out.println("VNC Set Sucessfully");

		} else {
			System.out.println("VNC Set Error:"+vncresult);

		}		 

	}
	
	
	public void setVmPxebootInterface(VirtualMachine vm, String interfaceName) throws InvalidName, VmConfigFault, DuplicateName, TaskInProgress, FileFault, InvalidState, ConcurrentAccess, InvalidDatastore, InsufficientResourcesFault, RuntimeFault, RemoteException, InterruptedException {
		//Set only boot from net on one of the interface (disable pxe)
		//http://virtualfoundry.blogspot.com/2009/05/secrets-of-e1000.html
		//http://kb.vmware.com/selfservice/microsites/search.do?language=en_US&cmd=displayKC&externalId=1014906
		//    ethernet0.opromsize = "0"
		//vlance.noOprom = "true" or vmxnet.noOprom = "true"
				
		//Read the macaddress generated by Vmware
		VirtualMachineConfigInfo vminfo = vm.getConfig();

		VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
		
		ArrayList<OptionValue> extraConfig=new ArrayList<OptionValue>();
		
		int e1000Counter=0;
		VirtualHardware hw= vminfo.getHardware();
		VirtualDevice [] devices = hw.getDevice();
		for(int i=0;i<devices.length;i++)
		{

			if (devices[i] instanceof VirtualEthernetCard) {

				String nicName=devices[i].getDeviceInfo().getLabel();
				

				String nicType="unknown";
				if (devices[i] instanceof VirtualE1000) {
					e1000Counter++;	
					nicType="e1000";
				}

				if (!nicName.toLowerCase().equals(interfaceName.toLowerCase())) {
					if (nicType=="e1000") {
						System.err.println("Disabling pxe on "+nicName+"-"+interfaceName);
						OptionValue nicSpec=new OptionValue() ;nicSpec.setKey("ethernet"+(e1000Counter-1)+".opromsize");nicSpec.setValue("0");
						extraConfig.add(nicSpec);

					}
					
					//Spec -> ethernet<counter-1>.opromsize=0
					
				} else {
					
				}

			}
		} 

		OptionValue optionConfig[]= extraConfig.toArray(new OptionValue[1]);
		
		vmConfigSpec.setExtraConfig(optionConfig);


		Task pxetask = 	vm.reconfigVM_Task(vmConfigSpec);
		String pxeresult=  	pxetask.waitForTask();

		if(pxeresult == Task.SUCCESS) 
		{
			System.out.println("Pxe Set Sucessfully");

		} else {
			System.out.println("Pxe Set Error:"+pxeresult);

		}
		
	}
	
	public void hardenVm(VirtualMachine vm) {

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

	}
	
	
	public void setBootOrderVm(VirtualMachine vm,String order) throws InvalidName, VmConfigFault, DuplicateName, TaskInProgress, FileFault, InvalidState, ConcurrentAccess, InvalidDatastore, InsufficientResourcesFault, RuntimeFault, RemoteException, InterruptedException {

		//bios.bootDeviceClasses
		//http://download3.vmware.com/sample_code/Perl/VMBootOrder.html
		// "allow:cd,hd,net"
		
		OptionValue bootOptions=new OptionValue() ;bootOptions.setKey("bios.bootDeviceClasses");bootOptions.setValue(order);
		OptionValue[] bootOptionsConfig= {  bootOptions };

		VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
		vmConfigSpec.setExtraConfig(bootOptionsConfig);

		Task bootOptionstask = 	vm.reconfigVM_Task(vmConfigSpec);
		String bootOptionsResult=  	bootOptionstask.waitForTask();

		if(bootOptionsResult == Task.SUCCESS) 
		{
			System.out.println("Boot order Set Sucessfully");

		} else {
			System.out.println("Boot order Error:"+bootOptionsResult);
		}
	

	}


	public String getMacAddress(String nicName, VirtualMachine newVm ) {

		//Read the macaddress generated by Vmware
		VirtualMachineConfigInfo vminfo = newVm.getConfig();
		String macAddress=null;

		VirtualHardware hw= vminfo.getHardware();
		VirtualDevice [] devices = hw.getDevice();
		for(int i=0;i<devices.length;i++)
		{

			if (devices[i] instanceof VirtualEthernetCard) {

				VirtualEthernetCard newnic=null;
				newnic=(VirtualEthernetCard) devices[i];

				String aName=devices[i].getDeviceInfo().getLabel();
				
				if (aName.toLowerCase().equals(nicName.toLowerCase())) {
					
					return(newnic.getMacAddress());
					
				}

			}
		} 

		return macAddress;
	}

	public void listNicsVm(VirtualMachine newVm) {

		//Read the macaddress generated by Vmware
		VirtualMachineConfigInfo vminfo = newVm.getConfig();

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

	}

	public VirtualMachine createVm(String vmName, long vmMemorySize, int vmCpuCount,
			String vmGuestOsId, VmDisk[] vmDisks,
			VmNic[] vmNics, String vmDataCenterName, String vmDataStoreName ) throws Exception {
		//	ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

		Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", vmDataCenterName);
		//    ResourcePool rp = (ResourcePool) new InventoryNavigator(
		//    dc).searchManagedEntities("ResourcePool")[0];
		ResourcePool rp = (ResourcePool) new InventoryNavigator(
				dc).searchManagedEntities("ResourcePool")[0];

		Folder vmFolder = dc.getVmFolder();

		// create vm config spec
		VirtualMachineConfigSpec vmSpec = 
			new VirtualMachineConfigSpec();
		vmSpec.setName(vmName);
		vmSpec.setAnnotation("Created by JvsphereControl");
		vmSpec.setMemoryMB(vmMemorySize);
		vmSpec.setNumCPUs(vmCpuCount);
		vmSpec.setGuestId(vmGuestOsId);

		//We create one scsi controller
		VirtualDeviceConfigSpec machineSpecs[]= new VirtualDeviceConfigSpec[vmNics.length+1+vmDisks.length];
		int cKey = 1000;
		VirtualDeviceConfigSpec scsiSpec = VsphereUtils.createScsiSpec(cKey);
		machineSpecs[0]=scsiSpec;
		
		// Associate the virtual disks with the scsi controller
		for (int i=0; i< vmDisks.length; i++) {
				VirtualDeviceConfigSpec diskSpec = VsphereUtils.createDiskSpec(
					vmDataStoreName, cKey, vmDisks[i].getSize(), vmDisks[i].getMode(),i);

			machineSpecs[i+1]=diskSpec;
			
		}
		
			
		//virtual network interfaces
		for (int i=0; i< vmNics.length; i++ ) {

			machineSpecs[vmDisks.length+1+i]= VsphereUtils.createNicSpec(
					vmNics[i].getName(), vmNics[i].getNetwork(),vmNics[i].isStartConnected(),vmNics[i].isConnected(),vmNics[i].getType());
		}   

		vmSpec.setDeviceChange(machineSpecs);

		
		// create vm file info for the vmx file
		VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
		vmfi.setVmPathName("["+ vmDataStoreName +"]");
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
			System.exit(-1);
		}		
		
		return newVm;
	}



	public void setCdromVm(VirtualMachine newVm, String vmCdromIsoPath,String vsphereDataStoreName) throws Exception {
		//Now add the cdrom

		System.err.println(vsphereDataStoreName);
		if (vmCdromIsoPath!=null) {
			VirtualDeviceConfigSpec cdSpec = VsphereUtils.createAddCdConfigSpec(newVm, vsphereDataStoreName, vmCdromIsoPath);
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
	}


	public void setEnterBiosVm(VirtualMachine newVm, boolean flag) throws InvalidName, VmConfigFault, DuplicateName, TaskInProgress, FileFault, InvalidState, ConcurrentAccess, InvalidDatastore, InsufficientResourcesFault, RuntimeFault, RemoteException, InterruptedException {

		VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

		// Enter bios for firs boot -> http://blogs.microsoft.co.il/blogs/scriptfanatic/archive/2009/08/27/force-a-vm-to-enter-bios-setup-screen-on-next-reboot.aspx

		VirtualMachineBootOptions bootoptions= new VirtualMachineBootOptions();
		bootoptions.setEnterBIOSSetup(flag);

		vmConfigSpec.setBootOptions(bootoptions);

		Task biostask = newVm.reconfigVM_Task(vmConfigSpec);
		String biosresult=biostask.waitForTask();

		if(biosresult == Task.SUCCESS) 
		{
			System.out.println("Enter Bios Set set to "+flag);

		} else {
			System.out.println("Enter Bios set error:"+biosresult);

		}

	}



}
