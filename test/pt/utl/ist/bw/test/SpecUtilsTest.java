package pt.utl.ist.bw.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.sf.saxon.instruct.Namespace;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.engine.YSpecificationID;

import com.google.gwt.benchmarks.client.Setup;

import pt.utl.ist.bw.utils.SpecUtils;
import pt.utl.ist.bw.utils.StringUtils;

public class SpecUtilsTest {
			
	private String yawlSpec;
	private String bwSpec;
	
	@Before
	public void setUp() {
		File medEp = new File("/tmp/MedicalEpisode.xml");
		this.yawlSpec = StringUtils.fileToString(medEp);
		
		File bw = new File("/tmp/BWMedicalEpisode.xml");
		this.bwSpec = StringUtils.fileToString(bw);
	}
	
	@Test
	public void testGetYAWLSpecVersion() {
		String version = SpecUtils.getYAWLSpecVersion(yawlSpec);
		
		Assert.assertEquals("0.2", version);
	}
	
	@Test
	public void testGetBWSpecID() {
		String specId = SpecUtils.getBWSpecificationIDFromSpec(bwSpec);
		
		Assert.assertNotNull(specId);
		
	}
	
	@Test
	public void testGetGoalSpecID() throws JDOMException, IOException {
		String goalSpecID = SpecUtils.getGoalSpecificationIDFromSpec(bwSpec);
		
		Assert.assertEquals("Medical_Episode_GSpec_0", goalSpecID);
	}
	
	@Test
	public void testGetConditionsSpecID() throws JDOMException, IOException {
		String condSpecID = SpecUtils.getConditionsspecificationIDFromSpec(bwSpec);
		
		Assert.assertEquals("Medical_Episode_CondSpec_0", condSpecID);
	}
	
	@Test
	public void testGetYAWLSpecificationIDFromSpec(){
		YSpecificationID ySpecID = SpecUtils.getYAWLSpecificationIDFromSpec(yawlSpec);
		
		Assert.assertNotNull(ySpecID);
		Assert.assertNotNull(ySpecID.getIdentifier());
		Assert.assertNotNull(ySpecID.getUri());
	}
	
	@Test
	public void testGetDataModelURIFromBWSpec() {
		String dataModelURI = SpecUtils.getDataModelURIFromBWSpec(bwSpec);
		
		Assert.assertEquals("MedicalEpisodeDataModel", dataModelURI);
	}
	
	@Test
	public void testGetBWSpecNameFromSpec() {
		String dataModelURI = SpecUtils.getBWSpecNameFromSpec(bwSpec);
		
		Assert.assertEquals("MedicalEpisodeBP", dataModelURI);
	}

}
