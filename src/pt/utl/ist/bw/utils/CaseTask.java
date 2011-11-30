package pt.utl.ist.bw.utils;

import java.util.ArrayList;

public class CaseTask {
	
	// IDs
	private int index = -1;
	private String taskName;
	private String taskID;
	private String taskDecompositionID; 
	
	//next tasks
	private ArrayList<String> nextTasksID = new ArrayList<String>();
	
	// parameters
	private ArrayList<Var> params = new ArrayList<Var>();
	
	public CaseTask(String taskName, int taskIndex) {
		if(taskName == null) {
			taskName = "";
		}
		if(taskIndex <= 2) {
			taskIndex = 3;
		}
		
		this.taskName = taskName;
		this.index = taskIndex;
		this.taskDecompositionID = this.taskName.replace(' ', '_');
		this.taskID = this.taskDecompositionID + "_" + this.index;
	}
	
	/////// GETTERS /////////
	public String getTaskName() { return taskName; }
	public String getTaskID() { return taskID; }
	public String getTaskDecompositionID() {return taskDecompositionID; }
	public ArrayList<String> getNextTasksID() {	return nextTasksID;	}
	public ArrayList<Var> getParams() {	return params; }
	//////////////////////////
	
	//////// SETTERS /////////
	public void setTaskName(String taskName) { this.taskName = taskName; }
	public void setTaskID(String taskID) { this.taskID = taskID; }
	public void setTaskDecompositionID(String taskDecompositionID) { this.taskDecompositionID = taskDecompositionID; }
	public void setNextTasksID(ArrayList<String> nextTasksID) {	this.nextTasksID = nextTasksID;	}
	public void setParams(ArrayList<Var> params) { this.params = params; }
	/////////////////////////
	
	public void addNextTask(String taskID) {
		this.nextTasksID.add(taskID);
	}
	
	public void setLastTask(boolean isLastTask) {
		if(isLastTask) {
			this.nextTasksID.clear();
			this.nextTasksID.add("OutputCondition_2");
		}
	}
	
	public boolean isLastTask() {
		if(this.nextTasksID.size() == 1 && this.nextTasksID.contains("OutputCondition_2")) {
			return true;
		}
		return false;
	}
}
