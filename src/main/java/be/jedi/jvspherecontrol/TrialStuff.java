package be.jedi.jvspherecontrol;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TrialStuff {

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
	String vmPxeInterface;
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

	//	http://www.java-opensource.com/open-source/command-line-interpreters.html
	void parseArgumentsTrial(String[]args2)throws Exception {

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


		vmActionOption.getArgName();

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

	}

	//	http://webcache.googleusercontent.com/search?q=cache:ds5QNEAJ_coJ:download3.vmware.com/sample_code/Java/CreateVMStaticMACAddress.html+get+mac+address+after+create+vm+java&cd=7&hl=nl&ct=clnk&gl=be&client=safari		

	// Mac generator = http://communities.vmware.com/thread/25126

	void parseArgumentsServer(String[] args)  throws Exception {

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

	void parseArgumentsClient(String[] args)  throws Exception {

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

		vmPxeInterface="Network Adapter 2";
		omapiHost="192.168.2.150";
		omapiPort=9991;
		omapiKeyName="omapi_key";
		omapiKeyValue="2YdVRKaJ4x41lDqHfA8rl8pHx95C4PmBgPcf5hIJ8j417HFN0AxUBEo6/3FoYyWjPyvXXCd+H6fPygtZd/iKxQ==";

	}
}
