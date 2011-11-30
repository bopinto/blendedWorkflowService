package pt.utl.ist.bw.conditionsinterpreter.conditioncompiler;

import pt.utl.ist.bw.conditionsinterpreter.conditions.AndCondition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.EqualToCondition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.ExistsCondition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.NotCondition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.TrueCondition;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;
import pt.utl.ist.bw.exceptions.InvalidConditionException;

public class ConditionParser {

	private final int STR_LENGHT;
	
	private static String _cond;
	private DataModelURI dataModelURI;
	private DataModelInstanceID instanceID;
	
	private int _token;
	
	public ConditionParser(String condition, DataModelURI dataModelURI, DataModelInstanceID instanceID) throws InvalidConditionException {
		if(condition == null || condition.equals("")) {
			throw new InvalidConditionException();
		}
		_cond = condition;
		STR_LENGHT = condition.length();
		this.dataModelURI = dataModelURI;
		this.instanceID = instanceID;
		_token = 0;
	}
	
	public Condition parseCondition() throws InvalidConditionException {
		Condition finalCondition = null;
		if(_cond.startsWith("exists(") || _cond.startsWith("equalTo(")) {
			finalCondition = parseConditionType();
		} else if(_cond.startsWith("true")){
			return new TrueCondition();
		} else {
			throw new InvalidConditionException();
		}
		
		return continueParseCondition(finalCondition);
	}
	
	protected Condition continueParseCondition(Condition parsedCondition) throws InvalidConditionException {
		while(_token < STR_LENGHT) {
			if(_cond.startsWith(" and ", _token) || _cond.startsWith(" or ", _token)) {
				parsedCondition = parseConditionJoiner(parsedCondition);
			} else {
				throw new InvalidConditionException();
			}
		}
		return parsedCondition;
	}
	
	////// PARSE RULES /////
	protected Condition parseConditionType() throws InvalidConditionException {
		Condition parsedCondition;
		if(_cond.startsWith("exists(", _token)) {
			parsedCondition = parseExistsCondition();
		} else if(_cond.startsWith("equalTo(", _token)) {
			parsedCondition = parseEqualToCondition();
		} else {
			return null;
		}
		
		// see if there is a .not() next
		if(_cond.startsWith(".not()", _token)) {
			parsedCondition = parseNotCondition(parsedCondition);
		}
		return parsedCondition;
	}
	
	protected Condition parseExistsCondition() throws InvalidConditionException {
		int endOfCondition = _cond.indexOf(')', _token);
		if(endOfCondition < _token) {
			throw new InvalidConditionException();
		}
		
		String existsString = _cond.substring(_token, endOfCondition+1);
		StringBuilder elementName  = new StringBuilder();
		int startArgs = "exists(".length();
		parseExistsConditionArgs(existsString, startArgs, existsString.length()-1, elementName);
		
		Condition existsCondition = new ExistsCondition(dataModelURI, new DataNameURI(elementName.toString()), instanceID);
		_token = endOfCondition+1;
		return existsCondition;
	}
	
	protected void parseExistsConditionArgs(String existsCondition, int startArgs, int endArgs, StringBuilder elementName) {
		if(startArgs > endArgs) return;
		
		elementName.append(existsCondition.substring(startArgs, endArgs));
	}
	
	protected Condition parseEqualToCondition() throws InvalidConditionException {
		int endOfCondition = _cond.indexOf(')', _token);
		
		if(endOfCondition < _token) {
			throw new InvalidConditionException();
		}
		
		String equaltoString = _cond.substring(_token, endOfCondition+1);
		StringBuilder elementName = new StringBuilder();
		StringBuilder elementValue = new StringBuilder();
		int startArgs = "equalTo(".length();
		parseEqualToConditionArgs(equaltoString, startArgs, equaltoString.length()-1, elementName, elementValue);
		
		Condition equalToCondition = new EqualToCondition(dataModelURI, new DataNameURI(elementName.toString()), instanceID, elementValue.toString());
		_token = endOfCondition+1;
		
		return equalToCondition;
	}
	
	protected void parseEqualToConditionArgs(String conditionString, int startArgs, int endArgs, StringBuilder elementName, StringBuilder elementValue) {
		if(startArgs > endArgs) return;
		
		int subToken = conditionString.indexOf(',', startArgs);
		
		elementName.append(conditionString.substring(startArgs, subToken));
		elementValue.append(conditionString.substring(subToken+1, endArgs).trim());
	}
	
	protected Condition parseNotCondition(Condition typeCondition) {
		_token += ".not()".length();
		return new NotCondition(typeCondition);
	}
	
	protected Condition parseConditionJoiner(Condition parsedCondition) throws InvalidConditionException {
		if(_cond.startsWith(" and ", _token)) {
			return parseAndCondition(parsedCondition);
		} else if(_cond.startsWith(" or ", _token)) {
			return parseOrCondition(parsedCondition);
		} else {
			throw new InvalidConditionException();
		}
	}
	
	protected Condition parseAndCondition(Condition parsedCondition) throws InvalidConditionException {
		_token += " and ".length();
		return new AndCondition(parsedCondition, parseConditionType());
	}
	
	protected Condition parseOrCondition(Condition parsedCondition) throws InvalidConditionException {
		_token += " or ".length();
		return new AndCondition(parsedCondition, parseConditionType());
	}
}
