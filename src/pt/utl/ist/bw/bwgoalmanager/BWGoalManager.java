package pt.utl.ist.bw.bwgoalmanager;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import pt.utl.ist.bw.conditionsinterpreter.ConditionsInterpreter;
import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.ConditionDBEntry;
import pt.utl.ist.bw.conditionsinterpreter.conditions.TripleStateBool;
import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.TaskID;
import pt.utl.ist.bw.elements.TaskURI;
import pt.utl.ist.bw.exceptions.UnregisteredConditionException;
import pt.utl.ist.bw.exceptions.WrongConditionException;
import pt.utl.ist.bw.messages.AddDataMessage;
import pt.utl.ist.bw.messages.AddRelationMessage;
import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.TaskInfo;
import pt.utl.ist.bw.utils.StringUtils;
import pt.utl.ist.bw.worklistmanager.CaseManager;
import pt.utl.ist.bw.worklistmanager.WorklistManager;
import pt.utl.ist.goalengine.EngineServerInterface;
import pt.utl.ist.goalengine.elements.GoalWorkItem;

/**
 * The service that handles the goal spec from Blended Workflow
 * @author bernardoopinto
 *
 */

public class BWGoalManager {
	
	// note: HashMap<idGoal, condition>
	private ArrayList<GoalWorkItem> activeGoals = new ArrayList<GoalWorkItem>();
	
	private ArrayList<String> runningCases = new ArrayList<String>();
	
	private Logger log = null;
	private static BWGoalManager instance = null;
	
	private BWGoalManager() {
		log = Logger.getLogger(BWGoalManager.class);
	}
	
	public static BWGoalManager get() {
		if(instance == null) {
			instance = new BWGoalManager();
		}
		return instance;
	}
	
	public String launchGoalCase(String goalSpecID, String dataModelURI) {
		// request the goal engine to start a goal case
		CaseURI goalCaseURI = new CaseURI(goalSpecID);
		DataModelURI dataModelName = new DataModelURI(dataModelURI);
		String instanceID = EngineServerInterface.get().launchGoalCase(goalSpecID);  
		if(instanceID != null) {
			this.runningCases.add(instanceID);
			ConditionsInterpreter.get().registerNewCase(dataModelName, goalCaseURI, new CaseInstanceID(instanceID), false);
		}
	
		return instanceID;
	}
	
	public boolean unloadGoalSpecification(String specID) {
		// send the request to the goal engine
		boolean unloadGoalSpec = EngineServerInterface.get().unloadSpecficiation(specID);
		
		// remove the conditions from the conditions db
		boolean unloadGoalDef = ConditionsDB.get().removeDefinitionSpec(new CaseURI(specID));
		
		return unloadGoalSpec && unloadGoalDef;
	}
	
	public boolean receiveGoalSpec(String specID, String specInString) {  //FIXME register a new case association in the condition interpreter
		// send the spec to the Goal Engine
		boolean loadGoalSpec = EngineServerInterface.get().loadSpecification(specInString);

		//send the 
		// send the conditions to the Conditions Engine
		boolean loadGoalDefs = false;

		// transform the spec into a document
		Document doc = StringUtils.stringToDoc(specInString);
		// get the root element
		Element root = doc.getRootElement();
		Namespace bwNs = root.getNamespace();
		
		//get the data model URI
		DataModelURI dataModelURI = new DataModelURI(root.getChildText("dataModelURI", bwNs));
		
		Element goalSpec = root.getChild("goalSpec", bwNs);

		CaseURI goalCase = new CaseURI(goalSpec.getAttributeValue("id"));
		
		
		// get the goalTree child
		Element goalTree = goalSpec.getChild("goalTree", bwNs);

		ArrayList<TaskID> goalList = ConditionsDB.get().insertNewSpec(new CaseURI(specID));

		// iterate over the goal children and get the goal definition
		for(int i=0; i < goalTree.getChildren().size(); i++) {
			Element goal = (Element) goalTree.getChildren().get(i);
			if(goal.getName().equals("goal")) {
				String goalID = goal.getChildText("id", bwNs);
				String goalDefinition = goal.getChildText("definition", bwNs);
				if(!ConditionsDB.get().storeGoalDefinition(goalCase, goalID, goalDefinition, dataModelURI)) {
					return false;
				} else {
					goalList.add(ConditionsDB.get().getGoalID(goalCase, goalID));
				}
			}
		}
		loadGoalDefs = true;
		return loadGoalSpec && loadGoalDefs;
	}
	
	public void notifyOfActiveGoals(String instanceID) {
		// clear all the goals in the interface
		for (GoalWorkItem goalWorkItem : this.activeGoals) {
			WorklistManager.get().removeGoalFromInterface(goalWorkItem.getIDForDisplay(), goalWorkItem.isGoalMandatory());
		}
		this.activeGoals.clear();
		// get the active goals
		ArrayList<GoalWorkItem> newActiveGoals = EngineServerInterface.get().getActiveGoals(instanceID); 
		
		this.activeGoals.addAll(newActiveGoals);
		// send the goal name to the worklist Manager
		
		for (GoalWorkItem goal : newActiveGoals) {
			TripleStateBool result = evaluateGoalDefinition(goal);
			if(result == TripleStateBool.TRUE) {
				EngineServerInterface.get().checkInGoal(goal);
				continue;
			}
			WorklistManager.get().registerActiveGoal(goal.getSpecificationInstanceID().toString(), goal.getGoalName(), goal.isGoalMandatory());
		}
		
	}
	
	/**
	 * Receives the data from a goal execution
	 * @param taskInfo the info from the goal execution
	 */
	public boolean receiveTaskExecutionResult(TaskInfo taskInfo) {
		GoalWorkItem gwir = null;
		for (GoalWorkItem activeWir : this.activeGoals) {
			if(activeWir.getIDForDisplay().equals(taskInfo.getTaskName())) {
				gwir = activeWir;
				break;
			}
		}
		
		if(gwir == null) {
			log.error("Receive Task execution result: Goal Work item does not exist");
			return false;
		}
		
		gwir.setGoalTaskInfo(taskInfo);
		if(EngineServerInterface.get().checkInGoal(gwir)) {
			this.activeGoals.remove(gwir);
		} else {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Gets the information to execute a goal
	 * @param goalURI the goal URI ([case instance]:[goal name])
	 * @return the info to execute the goal.
	 */
	public TaskInfo getInfoToExecuteGoal(String goalURI) {
		String[] goal = goalURI.split(":");
		String specInstance = goal[0];
		String goalName = goal[1];
		
		// get the info from the goal engine
		GoalWorkItem goalToExecute = null;
		for (GoalWorkItem g : this.activeGoals) { 
			if(g.getGoalName().equals(goalName) && g.getSpecificationInstanceID().toString().equals(specInstance)) {
				goalToExecute = g;
				break;
			}
		}
		if(goalToExecute == null) {
			log.error("Requested goal is not active");
			return null;
		}
	
		// get the condition from the conditions db
		try {
			//get the data model instance id
			DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(goalToExecute.getSpecificationInstanceID(), false);
			
			Condition goalDef = ConditionsDB.get().getGoalDefinition(goalToExecute.getSpecificationURI(), goalToExecute.getGoalID(), dataModelID);
			
			// get the data from the condition
			ArrayList<DataInfo> goalDefData = ConditionsInterpreter.get().getKeyDataFromCondition(goalDef);
			
			// get the task info
			TaskInfo taskInfo = new TaskInfo(TaskInfo.TASK_TYPE_GOAL, goalToExecute.getIDForDisplay());
			
			// fill the task info
			for (DataInfo dataInfo : goalDefData) {
				taskInfo.addInputData(dataInfo.getDataNameURI().getEntityName(), 
						dataInfo.getDataNameURI().getAttributeName(), 
						dataInfo.getDataModelURI(), dataModelID, 
						dataInfo.getDataType(), dataInfo.getRestrictions(), 
						dataInfo.getValue()); // we assume the data name is always [Entity].[Attribute]
				taskInfo.addOutputData(dataInfo.getDataNameURI().getEntityName(), 
						dataInfo.getDataNameURI().getAttributeName(),
						dataInfo.getDataModelURI(), dataModelID, 
						dataInfo.getDataType(), dataInfo.getRestrictions(), 
						dataInfo.getValue());
			}
			
			goalToExecute.setGoalTaskInfo(taskInfo);
			
			return taskInfo;
		} catch (UnregisteredConditionException e) {
			log.error("Goal definition is not registered. Goal: " + goalURI);
			return null;
		}
	}
		
	// skip goal
	public void skipGoal(String goalURI) {
		String[] goal = goalURI.split(":");
		String goalName = goal[1];
		
		GoalWorkItem wir = null;
		// get the work item
		for (GoalWorkItem g : this.activeGoals) { 
			if(g.getIDForDisplay().equals(goalURI)) {
				wir = g;
				break;
			}
		}
		if(wir == null) {
			log.error("Goal Work item is not active");
			return;
		}
			
		// send information to the Goal Engine
		EngineServerInterface.get().skipGoal(wir);

		try {
			// send information to the Conditions Engine
			DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(wir.getSpecificationInstanceID(), false);
			Condition goalCond = ConditionsDB.get().getGoalDefinition(wir.getSpecificationURI(), wir.getGoalID(), dataModelID);
			ConditionsInterpreter.get().skipData(goalCond);
		} catch (UnregisteredConditionException e) {
			log.error("The given goal work item does not have a registered condition");
		}
	}
	
	public void skipGoalEngineRequest(String goalURI, boolean isMandatory) {
		String[] goal = goalURI.split(":");
		String goalName = goal[1];
		
		GoalWorkItem wir = null;
		// get the work item
		for (GoalWorkItem g : this.activeGoals) { 
			if(g.getIDForDisplay().equals(goalURI)) {
				wir = g;
				break;
			}
		}
		if(wir == null) {
			log.error("Goal Work item is not active");
			return;
		}
		try {
			// send information to the Conditions Engine
			DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(wir.getSpecificationInstanceID(), false);
			Condition goalCond = ConditionsDB.get().getGoalDefinition(wir.getSpecificationURI(), wir.getGoalID(), dataModelID);
			ConditionsInterpreter.get().skipData(goalCond);
		} catch (UnregisteredConditionException e) {
			log.error("The given goal work item does not have a registered condition");
		}
		
		WorklistManager.get().removeGoalFromInterface(goalURI, isMandatory);
	}
	
	public void executeGoalEngineRequest(String goalURI, boolean isMandatory) {
		GoalWorkItem wir = null;
		// get the work item
		for (GoalWorkItem g : this.activeGoals) { 
			if(g.getIDForDisplay().equals(goalURI)) {
				wir = g;
				break;
			}
		}
		if(wir == null) {
			log.error("Goal Work item is not active");
			return;
		}
		
		this.activeGoals.remove(wir);
		
		WorklistManager.get().removeGoalFromInterface(goalURI, isMandatory);
	}
	
	// create goal
	// returns new goal ID
	public String createGoal(String instanceID, 
			String goalName, boolean isMandatory, 
			String goalDefinition, String parentGoalIDForDisplay) throws WrongConditionException {
		
		if(parentGoalIDForDisplay == null) return null;
		
		String activeGoal = CaseManager.get().getGoalURIFromGoalIDForDisplay(parentGoalIDForDisplay);

		GoalWorkItem parentGoal = null;
		for (GoalWorkItem gwir : this.activeGoals) {
			if(gwir.getIDForDisplay().equals(activeGoal)) {
				parentGoal = gwir;
				break;
			}
		}
		if(parentGoal == null) {
			log.error("Could not find the given goal work item");
			return null; //TODO throw exception
		}

		
		if(!ConditionsInterpreter.get().validateCondition(goalDefinition)) {
			throw new WrongConditionException();
		}
		
		
		// register in the goal engine
		String newGoalID = EngineServerInterface.get().addGoal(parentGoal.getSpecificationURI().toString(), 
				goalName, instanceID, isMandatory, goalDefinition, parentGoal.getGoalID());
		if(newGoalID == null) { return null; }
		
		// register in the condition DB
		ConditionsDB.get().storeGoalDefinition(parentGoal.getSpecificationURI(), newGoalID, goalDefinition, parentGoal.getDataModelURI());
		ConditionDBEntry dbEntry = ConditionsDB.get().getDBEntry(new TaskID(parentGoal.getSpecificationURI(), newGoalID));
		CaseInstanceID newGoalCaseInstanceID = new CaseInstanceID(instanceID);
		DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(newGoalCaseInstanceID, false);
		dbEntry.generateConditionInstance(dataModelID);
		
		// see if there are new goals that can be executed
		this.notifyOfActiveGoals(instanceID);
		
		return newGoalID;
	}
	
	public boolean addData(String dataModel, String dataModelInstanceID, 
			String entity, ArrayList<AddDataMessage> attributes, AddRelationMessage relation) {
		DataModelURI dataModelURI = new DataModelURI(dataModel);
		DataModelInstanceID instanceID = new DataModelInstanceID(dataModelInstanceID);
		
		return EngineServerInterface.get().addData(dataModelURI, instanceID, entity, attributes, relation);
	}
	
	public String getDataModel(CaseInstanceID goalCaseInstanceID) {
		if(goalCaseInstanceID == null) {
			return null;
		}
				
		DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(goalCaseInstanceID, false);
		DataModelURI dataModelURI = ConditionsInterpreter.get().getDataModelURI(goalCaseInstanceID, false);
		return ConditionsInterpreter.get().getDataModel(dataModelURI, dataModelID);
		
	}
	
	public String getDataModelInstanceID(CaseInstanceID goalCaseInstanceID) {
		return ConditionsInterpreter.get().getDataInstanceID(goalCaseInstanceID, false).toString();
	}
	
	public String getDataModelURI(CaseInstanceID goalCaseInstanceID) {
		return ConditionsInterpreter.get().getDataModelURI(goalCaseInstanceID, false).toString();
	}
	
	public void reevaluateGoalDefinition(TaskURI goalURI) {
		String goalIDForDisplay = goalURI.getTaskIDForDisplay();
		
		GoalWorkItem goal = null;
		for (GoalWorkItem gwir : this.activeGoals) {
			if(gwir.getID().equals(goalIDForDisplay)) {
				goal = gwir;
				break;
			}
		}
		if(goal == null) { return ; }
		
		TripleStateBool result = evaluateGoalDefinition(goal);
		if(result == TripleStateBool.TRUE) {
			EngineServerInterface.get().checkInGoal(goal);
		}
//		} else if(result == TripleStateBool.SKIPPED) {
//			if(isAllDataSkipped(goal)) {
//				EngineServerInterface.get().skipGoal(goal);
//				WorklistManager.get().removeGoalFromInterface(goalIDForDisplay, goal.isGoalMandatory());
//			}
//		}
	}
	
	private boolean isAllDataSkipped(GoalWorkItem goal) {
		Condition goalCond = getGoalCondition(goal);
		ArrayList<DataInfo> data = goalCond.getData();
		for (DataInfo dataInfo : data) {
			if(!dataInfo.isSkipped()) {
				return false;
			}
		}
		return true;
	}
	
	////// INTERNAL METHODS //////
	
	protected TripleStateBool evaluateGoalDefinition(GoalWorkItem goal) {
		Condition goalDef = getGoalCondition(goal);
		return ConditionsInterpreter.get().evaluateCondition(goalDef);
	}
	
	private Condition getGoalCondition(GoalWorkItem goal) {
		try {
			DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(goal.getSpecificationInstanceID(), false);
			return ConditionsDB.get().getGoalDefinition(goal.getSpecificationURI(), goal.getGoalID(), dataModelID);
		} catch (UnregisteredConditionException e) {
			log.error("Could not find condition for given goal");
			return null;
		}
	}
	
}
