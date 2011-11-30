package pt.utl.ist.bw.conditionsinterpreter;

import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.TripleStateBool;


/**
 * Evalutates a condition and returns the result
 * @author bernardoopinto
 *
 */
public class ConditionEvaluator {
	
	private ConditionEvaluator() {}

	public static TripleStateBool evaluateCondition(Condition condition) {
		return condition.evaluate();
	}
}
