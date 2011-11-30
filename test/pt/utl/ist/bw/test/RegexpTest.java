package pt.utl.ist.bw.test;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

import pt.utl.ist.bw.exceptions.InvalidConditionException;

public class RegexpTest {
	
	@Test
	public void conditionSplitTest() {
		String condition = "exists(Prescription.Recipe) and exists(Medical Report.Report)";
		String[] splittedConds = condition.split(" and | or ");
		
		Assert.assertTrue(splittedConds[0].equals("exists(Prescription.Recipe)"));
		Assert.assertTrue(splittedConds[1].equals("exists(Medical Report.Report)"));
	}
	
	@Test
	public void extractConditionTypeTest() {
		String condition = "exists(Medical Report.Report)";
		String[] splittedCond = condition.split("[(]|[)]");
		
		Assert.assertTrue(splittedCond[0].equals("exists"));
	}
	
	@Test
	public void extractEqualToConditionTest() {
		String condition = "equalsTo(Medical Report.Report, abc)";
		String[] splittedCond = condition.split("[(]|[)]");
		String data = splittedCond[1].split("[,]")[0];
		String value = splittedCond[1].split("[,]")[1].trim();
		
		Assert.assertTrue(splittedCond[0].equals("equalsTo"));
		Assert.assertTrue(data.equals("Medical Report.Report"));
		Assert.assertTrue(value.equals("abc"));
	}
	
	@Test
	public void testSubString() {
		String condition = "exists(Prescription.Recipe) and exists(Medical Report.Report)";
		int endOfCondition = condition.indexOf(')', 0);
		if(endOfCondition < 0) {
			fail();
		}
		
		String existsString;
		existsString = condition.substring(0, endOfCondition+1);
		
		Assert.assertEquals("exists(Prescription.Recipe)", existsString);
		int token = endOfCondition+1;
		
		String restOfCondition = condition.substring(token, condition.length());
		
		Assert.assertEquals(" and exists(Medical Report.Report)", restOfCondition);
		
	}

}
