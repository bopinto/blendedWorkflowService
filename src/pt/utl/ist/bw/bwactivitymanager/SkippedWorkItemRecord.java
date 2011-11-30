package pt.utl.ist.bw.bwactivitymanager;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

public class SkippedWorkItemRecord {

	private WorkItemRecord wir;
	private String preActivityCaseInstanceID;
	
	public WorkItemRecord getWorkItemRecord() {
		return wir;
	}
	public void setWorkItemRecord(WorkItemRecord wir) {
		this.wir = wir;
	}
	public String getPreActivityCaseInstanceID() {
		return preActivityCaseInstanceID;
	}
	public void setPreActivityCaseInstanceID(String preActivityCaseInstanceID) {
		this.preActivityCaseInstanceID = preActivityCaseInstanceID;
	}
	
	
}
