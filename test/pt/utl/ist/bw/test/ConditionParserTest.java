package pt.utl.ist.bw.test;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

import pt.utl.ist.bw.conditionsinterpreter.conditioncompiler.ConditionParser;
import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.exceptions.InvalidConditionException;

public class ConditionParserTest {

	private DataModelURI dataModelURI = new DataModelURI("TestDataModel");
	private DataModelInstanceID instanceID = new DataModelInstanceID("1");
	
	@Test
	public void testDoubleCondition() {
		String condition = "exists(Prescription.Recipe) and exists(Medical Report.Report)";
		Condition cond = null;
		try {
			cond = new ConditionParser(condition, dataModelURI, instanceID).parseCondition();
		} catch (InvalidConditionException e) {
			e.printStackTrace();
			fail();
		}
		Assert.assertNotNull(cond);
	}
	
	@Test
	public void testTripleCondition() {
		String condition = "exists(Prescription.Recipe) and exists(Medical Report.Report) or exists(Patient)";
		Condition cond = null;
		try {
			cond = new ConditionParser(condition, dataModelURI, instanceID).parseCondition();
		} catch (InvalidConditionException e) {
			e.printStackTrace();
			fail();
		}
		Assert.assertNotNull(cond);
	}
	
	@Test
	public void testNotCondition() {
		String condition = "exists(Prescription.Recipe).not() and exists(Medical Report.Report)";
		Condition cond = null;
		try {
			cond = new ConditionParser(condition, dataModelURI, instanceID).parseCondition();
		} catch (InvalidConditionException e) {
			e.printStackTrace();
			fail();
		}
		Assert.assertNotNull(cond);
		
		condition = "exists(Prescription.Recipe) and exists(Medical Report.Report).not()";
		cond = null;
		try {
			cond = new ConditionParser(condition, dataModelURI, instanceID).parseCondition();
		} catch (InvalidConditionException e) {
			e.printStackTrace();
			fail();
		}
		Assert.assertNotNull(cond);
	}
	
	@Test
	public void testEqualToCondition() {
		String condition = "equalTo(Medical Report.Closed,true)";
		Condition cond = null;
		try {
			cond = new ConditionParser(condition, dataModelURI, instanceID).parseCondition();
		} catch (InvalidConditionException e) {
			e.printStackTrace();
			fail();
		}
		Assert.assertNotNull(cond);
	}
}
