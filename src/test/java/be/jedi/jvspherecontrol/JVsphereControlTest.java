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
			//fail("it is missing a command");
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
			assertTrue(ex.getMessage().contains("url"));

		} catch (Exception ex) {
			System.out.println(ex.toString());			
			fail("it should send a missing required arguments exception");
		}	

	}


	/* This is a valid command createvm, has vsphere options but it misses the mandatory options for the create command */
	@Test
	public void testValidCommandCreateVmMissingCreateOptions() {
		String args[]= { "createvm","--url=https://esx.example.org/sdk","--user=esxadmin","--password=mypassword"};

		try {
			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
		} catch (InvalidCLICommandException ex) {
			fail("it should throw an invalid CLI command exception");
		} catch (MissingCLIArgumentException ex) {
			assertTrue(!ex.getMessage().contains("usernme"));
			assertTrue(!ex.getMessage().contains("url"));
			assertTrue(!ex.getMessage().contains("password"));
			assertTrue(ex.getMessage().contains("name"));

		} catch (Exception ex) {
			System.out.println(ex.toString());			
			fail("it should send a missing arguments exception");
		}	

	}

	@Test
	public void testValidCommandCreateVmMinimalOptions() {
		String args[]= { "createvm","--url=https://esx.example.org/sdk","--user=esxadmin","--password=mypassword",
				"--name=testvm", "--memory=256","--ostype=Ubuntu64Guest"				
		};

		try {

			JVsphereControl mockVsphereControl=createMock(JVsphereControl.class);
			expect(mockVsphereControl.execute()).andReturn(0);

			JVsphereControl vspherecontrol=new JVsphereControl(args);
			vspherecontrol.validateArgs(); 
		} catch (InvalidCLICommandException ex) {
			fail("it should throw an invalid CLI argument Syntax exception");
		} catch (MissingCLIArgumentException ex) {
			assertTrue(!ex.getMessage().contains("userame"));
			assertTrue(!ex.getMessage().contains("url"));
			assertTrue(!ex.getMessage().contains("password"));
			assertTrue(!ex.getMessage().contains("name"));

		} catch (Exception ex) {
			ex.printStackTrace();		
			fail("it should send a missing arguments exception");
		}	

	}

	@Test
	public void testValidCommandCreateVmMinimalOptionsBadMemory() {
		String args[]= { "createvm","--url=https://esx.example.org/sdk","--user=esxadmin","--password=mypassword",
				"--name=testvm", "--memory=2MMM","--ostype=Ubuntu64Guest", "--diskmode1=persistent", "--disksize1=100000"				
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
			assertTrue(ex.getMessage().contains("memory"));
		} catch (Exception ex) {
			System.out.println(ex.toString());			
			fail("it should send a missing arguments exception");
		}	

	}

	@Test
	public void testValidCommandCreateVmMinimalOptionsBadVsphereUrl() {
		String args[]= { "createvm","--url=httpppp://esx.example.org/sdk","--user=esxadmin","--password=mypassword",
				"--name=testvm", "--memory=256","--ostype=Ubuntu64Guest", "--diskmode1=persistent", "--disksize1=100000"			
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
			
			assertTrue(ex.getMessage().contains("url"));
		} catch (Exception ex) {

			fail("it should send a missing arguments exception");
		}	
	}


}
