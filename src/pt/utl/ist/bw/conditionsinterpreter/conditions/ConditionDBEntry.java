package pt.utl.ist.bw.conditionsinterpreter.conditions;

import java.util.HashMap;

import pt.utl.ist.bw.conditionsinterpreter.conditioncompiler.ConditionFactory;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.TaskID;
import pt.utl.ist.bw.exceptions.InvalidConditionException;

public class ConditionDBEntry {
	
	private DataModelURI dataModelURI;
	private TaskID parentID;
	private String rawCondition;
	private HashMap<DataModelInstanceID, Condition> conditionInstances = new HashMap<DataModelInstanceID, Condition>();
	
	public ConditionDBEntry(TaskID parentID, String condition, DataModelURI dataModelURI) {
		this.dataModelURI = dataModelURI;
		this.parentID = parentID;
		this.rawCondition = condition;
	}
	
	public Condition generateConditionInstance(DataModelInstanceID dataModelID) {
		if(dataModelID == null) {
			return null;
		}
		Condition cond = null;
		try {
			cond = ConditionFactory.createCondition(this.dataModelURI, this.rawCondition, dataModelID);
			conditionInstances.put(dataModelID, cond);
		} catch (InvalidConditionException e) {
			return null;
		}
		
		return cond;
	}

	public TaskID getParentID() { return this.parentID; }
	
	public Condition getInstance(DataModelInstanceID dataModelID) {
		return conditionInstances.get(dataModelID);
	}
}
