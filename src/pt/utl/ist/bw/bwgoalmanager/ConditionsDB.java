package pt.utl.ist.bw.bwgoalmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Repository for the goals' definition.
 * @author bernardoopinto
 *
 */
public class ConditionsDB {

	private HashMap<CaseURI, ArrayList<TaskID>> goalCases = new HashMap<CaseURI, ArrayList<TaskID>>(); //HashMap<case URI, List<idCase.idGoal>>
	private HashMap<TaskID, ConditionDBEntry> goalDefinitions = new HashMap<TaskID, ConditionDBEntry>(); // HashMap<idInstance.idCase.idGoal, condition>
	
	private static ConditionsDB instance = null;
	
	private ConditionsDB() {
	}
	
	public static ConditionsDB get() {
		if(instance == null) {
			instance = new ConditionsDB();
		}
		return instance;
	}
	
	public Map<TaskID, ConditionDBEntry> getDefinitions() { 
		return Collections.unmodifiableMap(this.goalDefinitions);
	}
	
	public ConditionDBEntry getDBEntry(TaskID goalID) {
		return this.goalDefinitions.get(goalID);
	}
	
	/**
	 * Gets a goal definition.
	 * @param caseInstanceID the goal case instance id
	 * @param goalCase the goal spec id (uri)
	 * @param goalName the goal id
	 * @param dataModelID the id of the data model
	 * @return the goal definition
	 * @throws UnregisteredConditionException if the goal is not registered in the conditions DB. 
	 */
	public Condition getGoalDefinition(CaseURI goalCase, 
			String goalName, DataModelInstanceID dataModelID) throws UnregisteredConditionException {
		if(goalCase == null || goalName == null || dataModelID == null) {
			return null;
		}
		
		TaskID goalID = getGoalID(goalCase, goalName);
		
		Condition goalDef = null;
		ConditionDBEntry dbEntry = this.goalDefinitions.get(goalID);
		
		if(dbEntry == null) {
			throw new UnregisteredConditionException();
		}
		
		if((goalDef = dbEntry.getInstance(dataModelID)) == null) {
			throw new UnregisteredConditionException();
		}
		return goalDef;
	}
	
	/**
	 * Stores a goal definition.
	 * @param goalCaseID the case instance ID.
	 * @param goalCase the goal spec id (uri)
	 * @param goalID the goal id
	 * @param goalDefinition the goal definition
	 * @return true if the goal is successfully stored and false otherwise
	 */
	public boolean storeGoalDefinition(CaseURI goalCaseURI, 
			String goalName, String goalDefinition, DataModelURI dataModelURI) {
		
		if(goalCaseURI == null || goalName == null || goalDefinition == null) {
			return false;
		}
		
		TaskID goalID = getGoalID(goalCaseURI, goalName);
		
		ConditionDBEntry cond = new ConditionDBEntry(goalID, goalDefinition, dataModelURI);
		this.goalDefinitions.put(goalID, cond);
		
		return true;
	}
	
	public boolean createNewInstance(CaseIDForDisplay caseIDForDisplay, DataModelInstanceID dataModelID) throws UnregisteredConditionException { //TODO test
		if(caseIDForDisplay == null || dataModelID == null) {
			return false;
		}
		
		ArrayList<TaskID> goalIDs = this.goalCases.get(caseIDForDisplay.getCaseURI());
		if(goalIDs == null) {
			return false;
		}
		Condition cond = null;
		for (TaskID goalID : goalIDs) {
			ConditionDBEntry entry = this.goalDefinitions.get(goalID);
			if(entry == null) {
				throw new UnregisteredConditionException();
			}
			cond = entry.generateConditionInstance(dataModelID);
			if(cond == null) {
				return false;
			}
			ConditionsInterpreter.get().registerCondition(getGoalURI(caseIDForDisplay, goalID), cond, "goal");
		}
		return true;
	}
	
	public ArrayList<TaskID> insertNewSpec(CaseURI specID) {
		if(specID == null) {
			return null;
		}
		
		ArrayList<TaskID> goalList = new ArrayList<TaskID>();
		
		this.goalCases.put(specID, goalList);
		
		return goalList;
	}
	
	public boolean removeDefinitionSpec(CaseURI specID) {
		ArrayList<TaskID> goalList = this.goalCases.get(specID);
		
		if(goalList == null) {
			return false;
		}
		
		for (TaskID goal : goalList) {
			this.goalDefinitions.remove(goal);
		}
		
		return true;
	}
	
	public TaskID getGoalID(CaseURI caseName, String goalName) {
		return new TaskID(caseName, goalName);
	}

	public TaskURI getGoalURI(CaseIDForDisplay caseIDForDisplay, TaskID goalID) {
		TaskURI goalURI = new TaskURI(caseIDForDisplay.getCaseInstanceID(), 
				caseIDForDisplay.getCaseURI(), goalID.getTaskName());
		return goalURI;
		
	}
}
