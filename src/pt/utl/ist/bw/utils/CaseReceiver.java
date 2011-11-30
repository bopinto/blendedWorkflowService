package pt.utl.ist.bw.utils;

import org.yawlfoundation.yawl.engine.YSpecificationID;

import pt.utl.ist.bw.BWCase;
import pt.utl.ist.bw.ControlPanel;
import pt.utl.ist.bw.worklistmanager.WorklistManager;

public class CaseReceiver {
	
	private ActivitySpecReceiver activitySpecReceiver = null;
	private BWSpecReceiver bwSpecReceiver = null;
	
	private BWCase bwCase = null;

	public CaseReceiver(ControlPanel ctrlPanel) {
		this.activitySpecReceiver = new ActivitySpecReceiver(ctrlPanel);
		this.bwSpecReceiver = new BWSpecReceiver(ctrlPanel);
	}
	
	public ActivitySpecReceiver getActivitySpecReceiver() {
		return this.activitySpecReceiver;
	}
	
	public BWSpecReceiver getBWSpecReceiver() {
		return this.bwSpecReceiver;
	}
	
	public void initCase(String caseName) {
		this.bwCase = new BWCase(caseName);
	}
	
	public String uploadSpec() {
		String yawlSpec = this.activitySpecReceiver.getSpecInString();
		String bwSpec = this.bwSpecReceiver.getSpecInString();
		String yawlSpecId = null;
		String yawlSpecName = null;
		String bwSpecId = null;
		String bwSpecURI = null;
		String goalSpecId = null;
		String condSpecId = null;
		String dataModelURI = null;
		
		if(yawlSpec == null || bwSpec == null) {
			return "Please submit all the specifications";
		}
		
		if(bwCase == null) {
			return "bwCase is null";
		}


		// get the spec id from the specs
		YSpecificationID ySpecID = SpecUtils.getYAWLSpecificationIDFromSpec(yawlSpec);
		yawlSpecId = ySpecID.getIdentifier();
		yawlSpecName = ySpecID.getUri();
		String yawlSpecVersion = SpecUtils.getYAWLSpecVersion(yawlSpec);
		bwSpecId = SpecUtils.getBWSpecificationIDFromSpec(bwSpec);
		bwSpecURI = SpecUtils.getBWSpecNameFromSpec(bwSpec);
		goalSpecId = SpecUtils.getGoalSpecificationIDFromSpec(bwSpec);
		condSpecId = SpecUtils.getConditionsspecificationIDFromSpec(bwSpec);
		dataModelURI = SpecUtils.getDataModelURIFromBWSpec(bwSpec);

		if(yawlSpecVersion == null || bwSpecURI == null || bwSpecId == null || yawlSpecId == null || goalSpecId == null || condSpecId == null || dataModelURI == null) {
			return "Could not retrieve the specification ID from both specifications";
		}
		
		// get the spec names and strings from the spec receivers and put them in the bwCase
		this.bwCase.setYAWLspec(yawlSpec);
		this.bwCase.setYAWLSpecVersion(yawlSpecVersion);
		this.bwCase.setBWspec(bwSpec);
		this.bwCase.setBWSpecName(bwSpecURI);
		this.bwCase.setYAWLSpecID(yawlSpecId);
		this.bwCase.setYAWLSpecName(yawlSpecName);
		this.bwCase.setBWSpecID(bwSpecId);
		this.bwCase.setGoalSpecID(goalSpecId);
		this.bwCase.setConditionsSpecID(condSpecId);
		this.bwCase.setDataModelURI(dataModelURI);
		
		// submit the case to the Worklist manager
		if(WorklistManager.get().receiveBWcase(bwCase)) {
			return null;	
		}
		return "Could not upload all the specifications correctly. Try again.";
	}

}
