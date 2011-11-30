package pt.utl.ist.bw.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.bw.exceptions.GoalHierarchyConsistencyException;
import pt.utl.ist.bw.utils.StringUtils;
import pt.utl.ist.goalengine.GoalEngine;
import pt.utl.ist.goalengine.elements.Goal;
import pt.utl.ist.goalengine.elements.GoalState;
import pt.utl.ist.goalengine.elements.GoalTree;
import pt.utl.ist.goalengine.elements.GoalWorkItem;

public class GoalTreeTest {
	
	private GoalTree goalTree;

	@Before
	public void setUp() {
		File f = new File("/tmp/BWMedicalEpisode.xml");
		String bwSpec = StringUtils.fileToString(f);
		Document doc = StringUtils.stringToDoc(bwSpec);
		goalTree = buildTreeFromDoc(doc);
	}
	
	@Test
	public void testAllGoalsLoaded() {
		Assert.assertNotNull("The is no Diagnose_Patient_0 goal", goalTree.getGoal("Diagnose_Patient_0"));
		Assert.assertNotNull("There is no Observe_Patient_1 goal", goalTree.getGoal("Observe_Patient_1"));
		Assert.assertNotNull("There is no Write_Medical_Report_2 goal", goalTree.getGoal("Write_Medical_Report_2"));
		Assert.assertNotNull("There is no Collect_Data_3 goal", goalTree.getGoal("Collect_Data_3"));
		Assert.assertNotNull("There is no Physical_Examination_4 goal", goalTree.getGoal("Physical_Examination_4"));
		Assert.assertNotNull("There is no Prescribe_5 goal", goalTree.getGoal("Prescribe_5"));
	}
	
	@Test
	public void testGetActiveGoals() {
		ArrayList<GoalWorkItem> goals = goalTree.getActiveGoals();
		
		Assert.assertEquals(5, goals.size());
	}
	
	@Test
	public void testGetInstanceID() {
	//should be null at the beginning
		Assert.assertNull(goalTree.getInstanceID());
		
		goalTree.setInstanceID(0);
		Assert.assertEquals("0", goalTree.getInstanceID());
	}
	
	@Test
	public void testGetRootGoal() {
		Assert.assertNotNull(goalTree.getRootGoal());
	}
	
	@Test
	public void testEvaluateTree() {
		goalTree.evaluateTree(goalTree.getRootGoal());
		
		Assert.assertEquals(GoalState.DEACTIVATED, goalTree.getGoal("Diagnose_Patient_0").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Observe_Patient_1").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Write_Medical_Report_2").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Collect_Data_3").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Physical_Examination_4").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Prescribe_5").getState());
	}
	
	@Test
	public void testExecuteNonMandatoryGoal() {
		goalTree.updateGoal("Collect_Data_3");
		
		Assert.assertEquals(GoalState.DEACTIVATED, goalTree.getGoal("Diagnose_Patient_0").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Observe_Patient_1").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Write_Medical_Report_2").getState());
		Assert.assertEquals(GoalState.EXECUTED, goalTree.getGoal("Collect_Data_3").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Physical_Examination_4").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Prescribe_5").getState());
	}
	
	@Test
	public void testExecuteMandatoryGoal() {
		goalTree.updateGoal("Observe_Patient_1");
		
		Assert.assertEquals(GoalState.DEACTIVATED, goalTree.getGoal("Diagnose_Patient_0").getState());
		Assert.assertEquals(GoalState.EXECUTED, goalTree.getGoal("Observe_Patient_1").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Write_Medical_Report_2").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Collect_Data_3").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Physical_Examination_4").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Prescribe_5").getState());
	}
	
	@Test
	public void testAddNonMandatoryGoal() {
		Goal g = goalTree.getGoal("Write_Medical_Report_2");
		Goal newGoal = new Goal(null, "New_Goal_6", "New Goal", "exists(Patient.Name)", false);
		try {
			goalTree.addSubGoal(newGoal, g);
		} catch (GoalHierarchyConsistencyException e) {
			fail();
		}
		
		Assert.assertEquals(GoalState.DEACTIVATED, goalTree.getGoal("Diagnose_Patient_0").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Observe_Patient_1").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Write_Medical_Report_2").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Collect_Data_3").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Physical_Examination_4").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Prescribe_5").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("New_Goal_6").getState());
	}
	
	@Test
	public void testSkipGoal() {
		goalTree.skipGoal("Observe_Patient_1");
		
		Assert.assertEquals(GoalState.DEACTIVATED, goalTree.getGoal("Diagnose_Patient_0").getState());
		Assert.assertEquals(GoalState.SKIPPED, goalTree.getGoal("Observe_Patient_1").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Write_Medical_Report_2").getState());
		Assert.assertEquals(GoalState.SKIPPED, goalTree.getGoal("Collect_Data_3").getState());
		Assert.assertEquals(GoalState.SKIPPED, goalTree.getGoal("Physical_Examination_4").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Prescribe_5").getState());
		
	}
	
	@Test
	public void testSkipGoalWithSubExecuted() {
		goalTree.updateGoal("Collect_Data_3");
		
		goalTree.skipGoal("Observe_Patient_1");
		
		Assert.assertEquals(GoalState.DEACTIVATED, goalTree.getGoal("Diagnose_Patient_0").getState());
		Assert.assertEquals(GoalState.SKIPPED, goalTree.getGoal("Observe_Patient_1").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Write_Medical_Report_2").getState());
		Assert.assertEquals(GoalState.EXECUTED, goalTree.getGoal("Collect_Data_3").getState());
		Assert.assertEquals(GoalState.SKIPPED, goalTree.getGoal("Physical_Examination_4").getState());
		Assert.assertEquals(GoalState.ACTIVATED, goalTree.getGoal("Prescribe_5").getState());
	}
	
	
	// this method is from pt.utl.ist.goalengine.EngineServerInterface
	protected GoalTree buildTreeFromDoc(Document doc) {
		Element docRoot = doc.getRootElement();
		Namespace bwns = docRoot.getNamespace();
		Element goalTreeElem = docRoot.getChild("goalSpec", bwns).getChild("goalTree", bwns);
		
		String dataModelURI = docRoot.getChildText("dataModelURI", bwns);
		
		// get the goals from the XML
		HashMap<String, Goal> goals = new HashMap<String, Goal>();
		for (Object goalObj : goalTreeElem.getChildren()) {
			if(((Element) goalObj).getName().equals("goal")) {
				Element goalXML = (Element) goalObj;
				Goal newGoal = new Goal(null, goalXML.getChild("id", bwns).getValue(),
						goalXML.getChild("name", bwns).getValue(), 
						goalXML.getChild("definition", bwns).getValue(),
						Boolean.parseBoolean(goalXML.getChild("mandatory", bwns).getValue()));
				if(goalXML.getChild("state", bwns).getValue().equals("Activated")) {
					newGoal.activate();
				}
				goals.put(newGoal.getID(), newGoal);
			}
		}
		
		// add the subgoals
		for (Object goalObj : goalTreeElem.getChildren()) {
			if(((Element) goalObj).getName().equals("goal")) {
				Element goalXML = (Element) goalObj;
				Goal g = goals.get(goalXML.getChild("id", bwns).getValue());
				for(int i=0; i<goalXML.getChild("subgoals", bwns).getChildren().size(); i++) {
					Element subgoalXML = (Element) goalXML.getChild("subgoals", bwns).getChildren().get(i);
					Goal subGoal = goals.get(subgoalXML.getValue());
					if(subGoal != null) {
						g.addSubGoal(subGoal);
					}
				}
			}
		}
		
		// get the specification id
		String specID = docRoot.getChild("goalSpec", bwns).getAttributeValue("id");
		// get the root goal
		Goal rootGoal = goals.get(goalTreeElem.getChild("rootgoal", bwns).getValue());
		// create the goal tree
		GoalTree goalTree = null;
		if(specID != null && rootGoal != null) {
			goalTree = new GoalTree(specID, rootGoal, dataModelURI);
		}
		
		return goalTree;
	}
}
