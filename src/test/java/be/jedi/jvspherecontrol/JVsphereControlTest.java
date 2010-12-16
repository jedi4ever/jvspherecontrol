package be.jedi.jvspherecontrol;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.junit.Test;

import be.jedi.jvspherecontrol.exceptions.InvalidCLIArgumentSyntaxException;
import be.jedi.jvspherecontrol.exceptions.InvalidCLICommandException;
import be.jedi.jvspherecontrol.exceptions.MissingCLIArgumentException;

public class JVsphereControlTest {

	@Test
	public void testInvalidCommand() {
		String args[]= { "nonexisting command"};

		try {
			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
			fail("it should throw an invalid CLI command exception");
		} catch (InvalidCLICommandException ex) {

		} catch (Exception ex) {
			fail("it is missing a command");
			System.out.println(ex.toString());			
		}			
	}

	/* This is a valid command createvm but it misses the mandatory options for the vsphere URL */
	@Test
	public void testValidCommandCreateVmMissingVsphereArgs() {
		String args[]= { "createvm"};

		try {
			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
		} catch (InvalidCLICommandException ex) {
			fail("it should send a missing required arguments exception");
		} catch (MissingCLIArgumentException ex) {	
			assertTrue(ex.getMessage().contains("vsphereUrl"));

		} catch (Exception ex) {
			System.out.println(ex.toString());			
			fail("it should send a missing required arguments exception");
		}	

	}


	/* This is a valid command createvm, has vsphere options but it misses the mandatory options for the create command */
	@Test
	public void testValidCommandCreateVmMissingCreateOptions() {
		String args[]= { "createvm","--vsphereUrl=https://esx.example.org","--vsphereUserName=esxadmin","--vspherePassword=mypassword"};

		try {
			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
		} catch (InvalidCLICommandException ex) {
			fail("it should throw an invalid CLI command exception");
		} catch (MissingCLIArgumentException ex) {
			assertTrue(!ex.getMessage().contains("vsphereUserName"));
			assertTrue(!ex.getMessage().contains("vsphereUrl"));
			assertTrue(!ex.getMessage().contains("vspherePassword"));
			assertTrue(ex.getMessage().contains("vmName"));

		} catch (Exception ex) {
			System.out.println(ex.toString());			
			fail("it should send a missing arguments exception");
		}	

	}

	@Test
	public void testValidCommandCreateVmMinimalOptions() {
		String args[]= { "createvm","--vsphereUrl=https://esx.example.org","--vsphereUserName=esxadmin","--vspherePassword=mypassword",
				"--vmName=testvm", "--vmMemorySize=256","--vmGuestOsId=Ubuntu64"				
		};

		try {

			JVsphereControl mockVsphereControl=createMock(JVsphereControl.class);
			expect(mockVsphereControl.execute()).andReturn(0);

			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
		} catch (InvalidCLICommandException ex) {
			fail("it should throw an invalid CLI argument Syntax exception");
		} catch (MissingCLIArgumentException ex) {
			assertTrue(!ex.getMessage().contains("vsphereUserName"));
			assertTrue(!ex.getMessage().contains("vsphereUrl"));
			assertTrue(!ex.getMessage().contains("vspherePassword"));
			assertTrue(!ex.getMessage().contains("vmName"));

		} catch (Exception ex) {
			ex.printStackTrace();		
			fail("it should send a missing arguments exception");
		}	

	}

	@Test
	public void testValidCommandCreateVmMinimalOptionsBadMemory() {
		String args[]= { "createvm","--vsphereUrl=https://esx.example.org","--vsphereUserName=esxadmin","--vspherePassword=mypassword",
				"--vmName=testvm", "--vmMemorySize=2MMM","--vmGuestOsId=Ubuntu64", "--vmDiskMode=persistent", "--vmDiskSize=100000"				
		};

		try {

			JVsphereControl mockVsphereControl=createMock(JVsphereControl.class);
			expect(mockVsphereControl.execute()).andReturn(0);

			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
		} catch (InvalidCLICommandException ex) {
			fail("it should throw an invalid CLI argument Syntax exception");
		} catch (MissingCLIArgumentException ex) {
			fail("it should throw an invalid CLI argument Syntax exception");
		} catch (InvalidCLIArgumentSyntaxException ex) {
			assertTrue(ex.getMessage().contains("vmMemorySize"));
		} catch (Exception ex) {
			System.out.println(ex.toString());			
			fail("it should send a missing arguments exception");
		}	

	}

	@Test
	public void testValidCommandCreateVmMinimalOptionsBadVsphereUrl() {
		String args[]= { "createvm","--vsphereUrl=httpppp://esx.example.org","--vsphereUserName=esxadmin","--vspherePassword=mypassword",
				"--vmName=testvm", "--vmMemorySize=256","--vmGuestOsId=Ubuntu64", "--vmDiskMode=persistent", "--vmDiskSize=100000"			
		};

		try {

			JVsphereControl mockVsphereControl=createMock(JVsphereControl.class);
			expect(mockVsphereControl.execute()).andReturn(0);

			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
		} catch (InvalidCLICommandException ex) {
			fail("it should throw an invalid CLI argument Syntax exception");
		} catch (MissingCLIArgumentException ex) {
			fail("it should throw an invalid CLI argument Syntax exception");
		} catch (InvalidCLIArgumentSyntaxException ex) {
			
			assertTrue(ex.getMessage().contains("vsphereUrl"));
		} catch (Exception ex) {

			fail("it should send a missing arguments exception");
		}	
	}


}
