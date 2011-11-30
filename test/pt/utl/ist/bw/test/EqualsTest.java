package pt.utl.ist.bw.test;

import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;

import pt.utl.ist.bw.elements.CaseIDForDisplay;
import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;

public class EqualsTest {

	@Test
	public void testCaseInstanceID() {
		CaseInstanceID instance1 = new CaseInstanceID("1");
		CaseInstanceID instance2 = new CaseInstanceID("2");
		CaseInstanceID instance11 = new CaseInstanceID("1");
		
		Assert.assertFalse(instance1.equals(instance2));
		Assert.assertTrue(instance1.equals(instance11));
	}
	
	@Test
	public void testCaseIDForDisplay() {
		// case id 1
		CaseInstanceID instance1 = new CaseInstanceID("1");
		CaseURI caseURI1 = new CaseURI("case");
		CaseIDForDisplay caseID1 = new CaseIDForDisplay(instance1, caseURI1);
	
		// case id 2
		CaseInstanceID instance2 = new CaseInstanceID("1");
		CaseURI caseURI2 = new CaseURI("case");
		CaseIDForDisplay caseID2 = new CaseIDForDisplay(instance2, caseURI2);
	
		Assert.assertTrue(caseID1.equals(caseID2));
	}
	
	@Test
	public void testEqualsFailDifferentInstanceID() {
		// case id 1
		CaseInstanceID instance1 = new CaseInstanceID("1");
		CaseURI caseURI1 = new CaseURI("case");
		CaseIDForDisplay caseID1 = new CaseIDForDisplay(instance1, caseURI1);
	
		// case id 2
		CaseInstanceID instance2 = new CaseInstanceID("2");
		CaseURI caseURI2 = new CaseURI("case");
		CaseIDForDisplay caseID2 = new CaseIDForDisplay(instance2, caseURI2);
	
		Assert.assertFalse(caseID1.equals(caseID2));
	}
	
	@Test
	public void testEqualsFailDifferentCaseURI() {
		// case id 1
		CaseInstanceID instance1 = new CaseInstanceID("1");
		CaseURI caseURI1 = new CaseURI("case");
		CaseIDForDisplay caseID1 = new CaseIDForDisplay(instance1, caseURI1);
	
		// case id 2
		CaseInstanceID instance2 = new CaseInstanceID("1");
		CaseURI caseURI2 = new CaseURI("case1");
		CaseIDForDisplay caseID2 = new CaseIDForDisplay(instance2, caseURI2);
	
		Assert.assertFalse(caseID1.equals(caseID2));
	}
	
	@Test
	public void testGetFromHashMap() {
		CaseInstanceID instance1 = new CaseInstanceID("1");
		CaseURI caseURI1 = new CaseURI("case");
		CaseIDForDisplay caseID1 = new CaseIDForDisplay(instance1, caseURI1);
	
		HashMap<CaseInstanceID, CaseIDForDisplay> cases = new HashMap<CaseInstanceID, CaseIDForDisplay>();
		cases.put(instance1, caseID1);
		
		CaseInstanceID instance2 = new CaseInstanceID("1");
		
		CaseIDForDisplay getcase = cases.get(instance2);
		
		Assert.assertTrue(getcase != null);
	}
}
