package pt.utl.ist.goalengine.elements;

import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.messages.TaskInfo;

public class GoalWorkItem {
	
	private CaseURI specificationURI;
	private CaseInstanceID specificationInstanceID;
	
	private DataModelURI dataModelURI;
	
	private String goalName;
	private String goalID;
	private String goalDefinition;
	private boolean isGoalMandatory;
	
	private TaskInfo goalTaskInfo = null;

	public GoalWorkItem(CaseURI specURI,
			CaseInstanceID specInstanceID,
			DataModelURI dataModelURI,
			String goalName,
			String goalID,
			String goalDef,
			boolean goalMandatory) {
		
		this.specificationURI = specURI;
		this.specificationInstanceID = specInstanceID;
		this.dataModelURI = dataModelURI;
		this.goalID = goalID;
		this.goalName = goalName;
		this.goalDefinition = goalDef;
		this.isGoalMandatory = goalMandatory;
	}
	
	/****** GETTERS ******/
	public CaseURI getSpecificationURI() { return specificationURI; }
	public CaseInstanceID getSpecificationInstanceID() { return specificationInstanceID; }
	public DataModelURI getDataModelURI() { return this.dataModelURI; }
	public String getGoalName() { return goalName; }
	public String getGoalID() { return this.goalID; }
	public String getGoalDefinition() { return goalDefinition; }
	public boolean isGoalMandatory() { return isGoalMandatory; }
	public String getIDForDisplay() { return this.specificationInstanceID + ":" + this.goalName; }
	public String getID() { return this.specificationInstanceID + ":" + this.goalID; }
	public TaskInfo getGoalTaskInfo() { return this.goalTaskInfo; }
	/*********************/

	/****** SETTERS ******/
	public void setSpecificationURI(CaseURI specificationURI) { this.specificationURI = specificationURI; }
	public void setSpecificationInstanceID(CaseInstanceID specificationInstanceID) { this.specificationInstanceID = specificationInstanceID; }
	public void setDataModelURI(DataModelURI uri) { this.dataModelURI = uri; }
	public void setGoalName(String goalName) { this.goalName = goalName; }
	public void setGoalDefinition(String goalDefinition) { this.goalDefinition = goalDefinition; }
	public void setGoalMandatory(boolean isGoalMandatory) { this.isGoalMandatory = isGoalMandatory; }
	public void setGoalTaskInfo(TaskInfo taskInfo) { this.goalTaskInfo = taskInfo; }
	/*********************/
}