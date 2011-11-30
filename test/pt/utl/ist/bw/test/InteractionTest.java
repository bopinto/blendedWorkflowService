package pt.utl.ist.bw.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.yawlfoundation.yawl.engine.YSpecificationID;

import pt.utl.ist.bw.BWCase;
import pt.utl.ist.bw.utils.SpecUtils;
import pt.utl.ist.bw.utils.StringUtils;
import pt.utl.ist.bw.worklistmanager.WorklistManager;

public class InteractionTest {
	
	private String yawlSpec = null;
	private String bwSpec = null;
	
	@Before
	public void setUp() {
		File f = new File("/tmp/MedicalEpisode.yawl");
		this.yawlSpec = StringUtils.fileToString(f);
		
		f = new File("/tmp/BWMedicalEpisode.xml");
		this.bwSpec = StringUtils.fileToString(f);
	}
	
	@Test
	public void testInteraction() {
		BWCase bwCase = emulateCaseReceiverUploadSpec();
		
		Assert.assertTrue(bwCase != null);
		
		// load case
		Assert.assertTrue(WorklistManager.get().receiveBWcase(bwCase));
		
		// launch case
		Assert.assertTrue(WorklistManager.get().launchCase(bwCase.getCaseName()));
		
		// handle enabled work items
		
	}
	
	protected BWCase emulateCaseReceiverUploadSpec() {
		BWCase bwCase = new BWCase("Medical Episode");
		
		String yawlSpecId = null;
		String yawlSpecName = null;
		String bwSpecId = null;
		String goalSpecId = null;
		String condSpecId = null;
		
		if(this.yawlSpec == null || this.bwSpec == null) {
			return null;
		}

		// get the spec id from the specs
		YSpecificationID ySpecID = SpecUtils.getYAWLSpecificationIDFromSpec(yawlSpec);
		yawlSpecId = ySpecID.getIdentifier();
		yawlSpecName = ySpecID.getUri();
		bwSpecId = SpecUtils.getBWSpecificationIDFromSpec(bwSpec);
		goalSpecId = SpecUtils.getGoalSpecificationIDFromSpec(bwSpec);
		condSpecId = SpecUtils.getConditionsspecificationIDFromSpec(bwSpec);

		if(bwSpecId == null || yawlSpecId == null || goalSpecId == null || condSpecId == null) {
			return null;
		}
		
		// get the spec names and strings from the spec receivers and put them in the bwCase
		bwCase.setYAWLspec(yawlSpec);
		bwCase.setBWspec(bwSpec);
		bwCase.setYAWLSpecID(yawlSpecId);
		bwCase.setYAWLSpecName(yawlSpecName);
		bwCase.setBWSpecID(bwSpecId);
		bwCase.setGoalSpecID(goalSpecId);
		bwCase.setConditionsSpecID(condSpecId);
		
		return bwCase;
	}

}
