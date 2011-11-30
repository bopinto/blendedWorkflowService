package pt.utl.ist.bw.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.bw.bwactivitymanager.ActivityEventRouter;
import pt.utl.ist.bw.utils.SpecUtils;
import pt.utl.ist.bw.utils.StringUtils;

public class ManagerEventRouterTest {
	
	private String specInString = null;
	private String specId = null;
	
	@Before
	public void setUp() {
		File f = new File("/tmp/MedicalEpisode.yawl");
		
		this.specInString = StringUtils.fileToString(f);
		
		this.specId = SpecUtils.getYAWLSpecificationIDFromSpec(this.specInString).getIdentifier();
		
	}
	
	@Test
	public void testLoadSpecification() {
		//test if I can load the spec
		Assert.assertTrue(ActivityEventRouter.getInstance().loadSpecification(this.specId, this.specInString, true));
		
		// test if I detect the spec is already loaded
		Assert.assertFalse(ActivityEventRouter.getInstance().loadSpecification(this.specId, this.specInString, true));
	}
	
	@Test
	public void testUnloadSpecification() {
		
		ActivityEventRouter.getInstance().loadSpecification(this.specId, this.specInString, true);
		
		Assert.assertTrue(ActivityEventRouter.getInstance().unloadSpecification(this.specId, true));
	}
	
	@Test
	public void launchSpecificationTest() {
		if(!ActivityEventRouter.getInstance().loadSpecification(this.specId, this.specInString, true)) {
			ActivityEventRouter.getInstance().unloadSpecification(this.specId, true);
			ActivityEventRouter.getInstance().loadSpecification(this.specId, this.specInString, true);
		}
		
		String instanceID = ActivityEventRouter.getInstance().launchYAWLcase(this.specId, true);
		
		Assert.assertTrue(instanceID != null);
	}
}
