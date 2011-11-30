package pt.utl.ist.bw.conditionsinterpreter;

import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;

public class CaseAssociation {
	
	private CaseURI activitySpecURI = null;
	private CaseInstanceID activityInstanceID = null;
	
	private CaseURI goalSpecURI = null;
	private CaseInstanceID goalInstanceID = null;
	
	private DataModelURI dataModelURI = null;
	private DataModelInstanceID dataModelInstanceID = null;
	
	public CaseAssociation(DataModelURI dataModelURI) {
		this.dataModelURI = dataModelURI;
	}

	//////// GETTERS /////////
	public CaseURI getActivitySpecURI() { return activitySpecURI; }
	public CaseInstanceID getActivityInstanceID() { return activityInstanceID; }
	
	public CaseURI getGoalSpecURI() { return goalSpecURI; }
	public CaseInstanceID getGoalInstanceID() { return goalInstanceID; }
	
	public DataModelURI getDataModelURI() { return dataModelURI; }
	public DataModelInstanceID getDataModelInstanceID() { return dataModelInstanceID; }
	/////////////////////////
	
	///////// SETTERS /////////
	public void setActivityURI(CaseURI uri) { this.activitySpecURI = uri; }
	public void setActivityInstanceID(CaseInstanceID activityInstanceID) { this.activityInstanceID = activityInstanceID; }
	
	public void setGoalSpecURI(CaseURI uri) { this.goalSpecURI = uri; }
	public void setGoalInstanceID(CaseInstanceID goalInstanceID) { this.goalInstanceID = goalInstanceID; }
	
	public void setDataModelURI(DataModelURI uri) { this.dataModelURI = uri; }
	public void setDataModelInstanceID(DataModelInstanceID dataModelInstanceID) { this.dataModelInstanceID = dataModelInstanceID; }
	//////////////////////////
}
