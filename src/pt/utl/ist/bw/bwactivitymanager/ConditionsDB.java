package pt.utl.ist.bw.bwactivitymanager;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.bw.conditionsinterpreter.ConditionsInterpreter;
import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.ConditionDBEntry;
import pt.utl.ist.bw.elements.CaseIDForDisplay;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.TaskID;
import pt.utl.ist.bw.elements.TaskURI;
import pt.utl.ist.bw.exceptions.UnregisteredConditionException;

/**
 * Repository for the activities' pre and post-conditions. 
 * @author bernardoopinto
 *
 */
public class ConditionsDB {

	private HashMap<CaseURI, ArrayList<TaskID>> caseDB = new HashMap<CaseURI, ArrayList<TaskID>>(); //HashMap<caseIDForDisplay, List<idCase.idActivity>>
	private HashMap<TaskID, ConditionDBEntry> preconditions = new HashMap<TaskID, ConditionDBEntry>(); // HashMap<idCase.idActivity, condition>
	private HashMap<TaskID, ConditionDBEntry> postconditions = new HashMap<TaskID, ConditionDBEntry>(); // HashMap<idCase.idActivity, condition>
	
	private static ConditionsDB instance = null;
	
	private ConditionsDB() {}
	
	public static ConditionsDB get() {
		if(instance == null) {
			instance = new ConditionsDB();
		}
		return instance;
	}
	
	public Condition getPrecondition(CaseURI caseName, 
			String taskName, DataModelInstanceID dataModelID) throws UnregisteredConditionException {
		
		if(caseName == null || taskName == null || dataModelID == null) {
			return null;
		}
		
		Condition preCond = null;
		TaskID taskID = getTaskID(caseName, taskName);
		
		if((preCond = this.preconditions.get(taskID).getInstance(dataModelID)) == null) {
			throw new UnregisteredConditionException();
		}
		return preCond;
	}
	
	public Condition getPostcondition(CaseURI caseName, String taskName, DataModelInstanceID dataModelID) throws UnregisteredConditionException {
		if(caseName == null || taskName == null || dataModelID == null) {
			return null;
		}
		
		Condition postCond = null;
		TaskID taskID = getTaskID(caseName, taskName);
		
		if((postCond = this.postconditions.get(taskID).getInstance(dataModelID)) == null) {
			throw new UnregisteredConditionException();
		}
		return postCond;
	}
	
	public boolean addPrecondition(CaseURI caseName, String taskName, String preCondition, DataModelURI dataModelURI) {
		if(caseName == null || taskName == null || preCondition == null) {
			return false;
		}
		
		TaskID taskID = getTaskID(caseName, taskName);
		ConditionDBEntry cond = new ConditionDBEntry(taskID, preCondition, dataModelURI);
		
		this.preconditions.put(taskID, cond);
		return true;
	}
	
	public boolean removePrecondition(CaseURI caseName, String taskName) {
		if(caseName == null || taskName == null) {
			return false;
		}
		TaskID taskID = getTaskID(caseName, taskName);
		this.preconditions.remove(taskID);
		return true;
	}
	
	public boolean addPostcondition(CaseURI caseName, 
			String taskName, String postcondition, DataModelURI dataModelURI) {
		
		if(caseName == null || taskName == null || postcondition == null) {
			return false;
		}
		
		TaskID taskID = getTaskID(caseName, taskName);
		ConditionDBEntry cond = new ConditionDBEntry(taskID, postcondition, dataModelURI);
		
		this.postconditions.put(taskID, cond);
		return true;
	}
	
	public boolean createNewPreConditionInstance(CaseIDForDisplay caseIDForDisplay, DataModelInstanceID dataModelID) { //TODO test
		if(caseIDForDisplay == null || dataModelID == null) {
			return false;
		}
		
		ArrayList<TaskID> activities = this.caseDB.get(caseIDForDisplay.getCaseURI());
		if(activities == null) {
			return false;
		}
		
		for (TaskID activityID : activities) {
			ConditionDBEntry entry = this.preconditions.get(activityID);
			Condition cond = entry.generateConditionInstance(dataModelID); 
			if(cond == null) {
				return false;
			}
			ConditionsInterpreter.get().registerCondition(getTaskURI(caseIDForDisplay, activityID), cond, "pre");
		}
		return true;
	}
	
	public boolean createNewPostConditionInstance(CaseIDForDisplay caseIDForDisplay, DataModelInstanceID dataModelID) { //TODO test
		if(caseIDForDisplay == null || dataModelID == null) {
			return false;
		}
		
		ArrayList<TaskID> activities = this.caseDB.get(caseIDForDisplay.getCaseURI());
		if(activities == null) {
			return false;
		}
		
		for (TaskID activityID : activities) {
			ConditionDBEntry entry = this.postconditions.get(activityID);
			Condition cond = entry.generateConditionInstance(dataModelID);
			if(cond == null) {
				return false;
			}
			ConditionsInterpreter.get().registerCondition(getTaskURI(caseIDForDisplay, activityID), cond, "post");
		}
		return true;
	}
	
	public boolean removePostcondition(CaseURI caseName, String taskName) { 
		if(caseName == null || taskName == null) {
			return false;
		}
		
		TaskID taskID = getTaskID(caseName, taskName);
		this.postconditions.remove(taskID);
		return true;
	}
	
	public ArrayList<TaskID> insertNewSpec(CaseURI caseURI) {
		if(caseURI == null) {
			return null;
		}
		
		ArrayList<TaskID> activityList = new ArrayList<TaskID>();
		
		this.caseDB.put(caseURI, activityList);
		
		return activityList;
	}
	
	public boolean removeSpecification(CaseURI caseURI) { //TODO remove register from the ConditionEngine
		ArrayList<TaskID> activityList = this.caseDB.get(caseURI);
		
		if(activityList == null) {
			return false;
		}
		
		for (TaskID task : activityList) {
			this.preconditions.remove(task);
			this.postconditions.remove(task);
		}
		
		this.caseDB.remove(caseURI);
		
		return true;
	}
	
	public TaskID getTaskID(CaseURI caseName, String taskName) {
		return new TaskID(caseName, taskName);
	}
	
	public TaskURI getTaskURI(CaseIDForDisplay caseIDForDisplay, TaskID taskID) {
		TaskURI taskURI = new TaskURI(caseIDForDisplay.getCaseInstanceID(), 
				caseIDForDisplay.getCaseURI(), taskID.getTaskName());
		return taskURI;
	}
}
