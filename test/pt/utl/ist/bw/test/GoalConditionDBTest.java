package pt.utl.ist.bw.test;

import org.junit.Test;

import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelURI;

public class GoalConditionDBTest {

	@Test
	public void testStoreGoalDefinition() {
		CaseURI caseURI = new CaseURI("Medical_Episode_GSpec_0");
		String goalName = "Diagnose Patient";
		String goalDefinition = "exists(Patient) and exists(Medical Report) and equalTo(Medical Report.Closed,true)";
		DataModelURI dataModelURI = new DataModelURI("MedicalEpisodeDataModel");
	}
	
	@Test
	public void testGetDBEntry() {
		
	}
	
	@Test
	public void testGetGoalDefinition() {
		
	}
	
	@Test
	public void testCreateNewInstance() {
		
	}
	
	@Test
	public void testInsertNewSpec() {
		//TODO add goals and then see if inside the DB they are there.
	}
	
	@Test
	public void testRemoveDefinitionSpec() {
		
	}
	
	@Test
	public void testGetGoalID() {
		
	}
}
