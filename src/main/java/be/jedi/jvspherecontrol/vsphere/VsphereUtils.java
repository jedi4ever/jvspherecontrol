package be.jedi.jvspherecontrol.vsphere;

import com.vmware.vim25.ConfigTarget;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.Description;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDeviceConnectInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualFloppy;
import com.vmware.vim25.VirtualFloppyImageBackingInfo;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigOption;
import com.vmware.vim25.VirtualMachineDatastoreInfo;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.VirtualVmxnet2;
import com.vmware.vim25.VirtualVmxnet3;
import com.vmware.vim25.mo.EnvironmentBrowser;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.VirtualMachine;

public class VsphereUtils {

	public static  VirtualDeviceConfigSpec createScsiSpec(int cKey)
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



	public static  VirtualDeviceConfigSpec createDiskSpec(String dsName, 
			int cKey, long diskSizeKB, String diskMode,int unitNumber)
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
		vd.setUnitNumber(unitNumber);
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



	static public VirtualDeviceConfigSpec createFloppySpec(VirtualMachine vm, String dsName,  String floppyName) throws Exception {

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
	static public VirtualDeviceConfigSpec createNicSpec(String netName, 
			String nicName,boolean startConnected,boolean connected,String nicAdapter) throws Exception
			{


		//		
		//	http://communities.vmware.com/message/1251528
		VirtualDeviceConfigSpec nicSpec = 
			new VirtualDeviceConfigSpec();
		nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

		VirtualEthernetCard nic=null;
		
		if (nicAdapter.equals("e1000")) {
			nic= new VirtualE1000();
		}
		if (nicAdapter.equals("pcnet32")) {
			nic= new VirtualPCNet32();
		}
		if (nicAdapter.equals("vmxnet2")) {
			nic= new VirtualVmxnet2();
		}
		if (nicAdapter.equals("vmxnet3")) {
			nic= new VirtualVmxnet3();
		}
		
		if (nic==null) {
			throw new Exception("unknown nic adaptor type");
		}
		
		
		
		nic.setConnectable(new VirtualDeviceConnectInfo());
		nic.connectable.setStartConnected(startConnected);
		nic.connectable.setConnected(connected);



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

	static public VirtualDeviceConfigSpec createAddCdConfigSpec(VirtualMachine vm, String dsName, String isoName) throws Exception 
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

	static public VirtualDeviceConfigSpec createRemoveCdConfigSpec(VirtualMachine vm, String cdName) throws Exception 
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

	private static  VirtualDevice findVirtualDevice(
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

	static public DatastoreSummary findDatastoreSummary(VirtualMachine vm, String dsName) throws Exception 
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

	static public VirtualDevice getIDEController(VirtualMachine vm) 
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

	static  public VirtualDevice[] getDefaultDevices(VirtualMachine vm) 
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
