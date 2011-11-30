package pt.utl.ist.bw.conditionsinterpreter.conditioncompiler;

import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.exceptions.InvalidConditionException;

/**
 * Creates and returns new conditions
 * @author bernardoopinto
 *
 */
public class ConditionFactory {
	
	public static boolean isConditionValid(String rawCondition) {
		 //TODO parse the raw condition looking for errors
		return true;
	}
	
	public static Condition createCondition(DataModelURI dataModelURI, String condInString, DataModelInstanceID instanceID) throws InvalidConditionException {
		return new ConditionParser(condInString, dataModelURI, instanceID).parseCondition();
	}
}
