package pt.utl.ist.goalengine;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.messages.AddDataMessage;
import pt.utl.ist.bw.messages.AddRelationMessage;
import pt.utl.ist.bw.utils.StringUtils;
import pt.utl.ist.goalengine.elements.Goal;
import pt.utl.ist.goalengine.elements.GoalTree;
import pt.utl.ist.goalengine.elements.GoalWorkItem;

/**
 * Handles the events where the engine is the server (receives events)
 * @author bernardoopinto
 *
 */
public class EngineServerInterface {

	private static EngineServerInterface instance = null;
	private static Logger log = null;
	
	private EngineServerInterface() {
		log = Logger.getLogger(EngineServerInterface.class);
	}
	
	public static EngineServerInterface get() {
		if(instance == null) {
			instance = new EngineServerInterface();
		}
		return instance;
	}
	
	public String launchGoalCase(String goalSpecID) {
		if(GoalEngine.get().getGoalTree(goalSpecID) != null) {
			return GoalEngine.get().startGoalCase(goalSpecID);
		} else return null;
	}
	
	public boolean unloadSpecficiation(String specID) {
		GoalTree goalTree = GoalEngine.get().getGoalTree(specID);
		
		if(goalTree == null) {
			log.error("Goal tree is not registered in the system");
			return false;
		}
		
		GoalEngine.get().removeGoalTree(specID);
		
		return true;
	}
	
	/**
	 * Loads a Goal Specification into the Goal Engine
	 * @param spec the blended workflow specification in XML format
	 * @return true if the specification was correctly loaded or false otherwise.
	 */
	public boolean loadSpecification(String spec) {
		// parse the specification string (string -> document)
		Document doc = StringUtils.stringToDoc(spec);

		// parse the specification doc (doc -> goaltree)
		GoalTree goalTree = buildTreeFromDoc(doc);
		// load the specification in the engine
		if(goalTree != null) {
			GoalEngine.get().addGoalTree(goalTree);
			return true;
		} else {
			log.error("Could not build the goal tree");
			//System.out.println("Could not build the goal tree"); //FIXME remove: just for testing purposes
			return false;
		}
	}
	
	public ArrayList<GoalWorkItem> getActiveGoals(String instanceID) {
		return GoalEngine.get().getActiveGoals(instanceID);
	}
	

	public boolean checkInGoal(GoalWorkItem gwir) {
		return GoalEngine.get().checkInGoal(gwir);
	}
	
	public boolean skipGoal(GoalWorkItem gwir) {
		return GoalEngine.get().skipGoal(gwir);
	}
	
	// returns the goal id
	public String addGoal(String treeID, String goalName, String instanceID, boolean isMandatory, String goalDefinition, String parentID) {
		return GoalEngine.get().addGoal(treeID, goalName, instanceID, isMandatory, goalDefinition, parentID);
	}
	
	public boolean addData(DataModelURI dataModelURI, DataModelInstanceID instanceID, String entity, ArrayList<AddDataMessage> attributes, AddRelationMessage relation) {
		return GoalEngine.get().addData(dataModelURI, instanceID, entity, attributes, relation);
	}
	
	//////////// INTERNAL METHODS //////////////
	
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
