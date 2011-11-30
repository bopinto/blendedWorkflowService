package pt.utl.ist.goalengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;
import pt.utl.ist.bw.exceptions.GoalHierarchyConsistencyException;
import pt.utl.ist.bw.messages.AddDataMessage;
import pt.utl.ist.bw.messages.AddRelationMessage;
import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.EntityInfo;
import pt.utl.ist.bw.messages.GroupDataInfo;
import pt.utl.ist.bw.messages.TaskInfo;
import pt.utl.ist.goalengine.elements.Goal;
import pt.utl.ist.goalengine.elements.GoalTree;
import pt.utl.ist.goalengine.elements.GoalWorkItem;

/**
 * The Goal Engine
 * Provides the information about how to create a goal.
 * @author bernardoopinto
 *
 */
public class GoalEngine {
	
	private HashMap<String, GoalTree> goalTrees = new HashMap<String, GoalTree>(); //key = goal tree ID.
	private ArrayList<GoalTree> activeGoalCases = new ArrayList<GoalTree>();
	
	private int treeInstanceGen = 0;
	
	private static GoalEngine instance = null;
	private GoalEngineDBGateway dbGateway = null;
	private Logger log;
	
	private GoalEngine() {
		log = Logger.getLogger(GoalEngine.class);
		this.dbGateway = new GoalEngineDBGateway("http://localhost:8080/dataRepository/bw");
	}
	
	public static GoalEngine get() {
		if(instance == null) {
			instance = new GoalEngine();
		}
		return instance;
	}
	
	public void addGoalTree(GoalTree gt) { this.goalTrees.put(gt.getTreeID(), gt); }
	
	public GoalTree getGoalTree(String treeURI) {
		return this.goalTrees.get(treeURI);
	}
	
	public void removeGoalTree(String treeURI) {
		this.goalTrees.remove(treeURI);
	}
	
	public String startGoalCase(String treeURI) {
		// get the goaltree
		GoalTree goalTree = getGoalTree(treeURI);
		if(goalTree == null) {
			log.error("Could not find the specified goal tree. TreeID: " + treeURI);
			return null;
		}
		
		this.activeGoalCases.add(goalTree);
		goalTree.run();
		goalTree.setInstanceID(treeInstanceGen++);
		
		return goalTree.getInstanceID();
	}
	
	public ArrayList<GoalWorkItem> getActiveGoals(String instanceID) {
		if(instanceID == null) {
			return new ArrayList<GoalWorkItem>();
		}
		GoalTree goalTree = null;
		
		for (GoalTree gt : this.activeGoalCases) {
			if(gt.getInstanceID().equals(instanceID)) {
				goalTree = gt;
				break;
			}
		}
		if(goalTree == null) {
			return new ArrayList<GoalWorkItem>();
		}
		
		return goalTree.getActiveGoals();
	}
	
	public boolean checkInGoal(GoalWorkItem gwir) {
		GoalTree gTree = null;
		for (GoalTree gt : this.activeGoalCases) {
			if(gt.getInstanceID().equals(gwir.getSpecificationInstanceID().toString())) {
				gTree = gt;
				break;
			}
		}
		if(gTree == null) {
			log.error("Could not find the case for the given work item");
			return false;
		}
		gTree.updateGoal(gwir.getGoalID());
		
		// submit the data to the Data Repository
		try {
			TaskInfo goalTaskInfo = gwir.getGoalTaskInfo();
			if(goalTaskInfo != null) {
			for (GroupDataInfo gDataInfo : goalTaskInfo.getOutputData()) {
				for (DataInfo dataInfo : gDataInfo.getFields()) {
					this.dbGateway.submitData(dataInfo);
				}
			}
			} else { // this means it was checked in without being executed.
				return true;
			}
		} catch(IOException ioe) {
			log.error("Could not submit all data");
			return false;
		}
		
		EngineClientInterface.get().notifyOfActiveGoals(gTree.getInstanceID());
		
		return true;
	}
	
	public boolean skipGoal(GoalWorkItem gwir) {
		GoalTree gTree = null;
		
		for (GoalTree gt : this.activeGoalCases) {
			if(gt.getInstanceID().equals(gwir.getSpecificationInstanceID().toString()) && gt.getTreeID().equals(gwir.getSpecificationURI().toString())) {
				gTree = gt;
				break;
			}
		}
		if(gTree == null) {
			log.error("Could not find the case for the given work item");
			return false;
		}
		gTree.skipGoal(gwir.getGoalID());
			
		
		return true;
	}
	
	public void treeUpdate(String instanceID) {
		EngineClientInterface.get().notifyOfActiveGoals(instanceID);
	}
	
	// returns the goal id
	public String addGoal(String treeID, String goalName, String instanceID, boolean isMandatory, String goalDefinition, String parentID) {
		GoalTree gTree = null;
		String idForDisplay = instanceID + ":" + treeID;
		for (GoalTree gt : this.activeGoalCases) {
			if(gt.getIDForDisplay().equals(idForDisplay)) {
				gTree = gt;
			}
		}
		if(gTree == null) {
			return null;
		}
		
		Goal newGoal = new Goal(gTree, null, goalName, goalDefinition, isMandatory);
		
		String goalID = null;
		try {
			goalID = gTree.addSubGoal(newGoal, gTree.getGoal(parentID));
		} catch(GoalHierarchyConsistencyException e) {
			log.error("Could not add the subgoal. An hierarchic exception occured.");
		}
		
		return goalID;
	}
	
	public boolean addData(DataModelURI dataModelURI, DataModelInstanceID instanceID, String elementURI, 
			ArrayList<AddDataMessage> attributes, 
			AddRelationMessage relation) {
		try {
			if(elementURI.split("\\.").length == 2) {
				// we are only submitting a new attribute to an entity
				AddDataMessage addDataMessage = attributes.get(0);
				DataInfo att = new DataInfo();
				att.setDataModelURI(dataModelURI);
				att.setInstanceID(instanceID);
				att.setDataNameURI(new DataNameURI(elementURI));
				att.setDataType(addDataMessage.getDataType());
				att.isDefined(false);
				att.isSkipped(false);
				att.isKey(addDataMessage.isKey());
				if(!this.dbGateway.addData(att, null)) {
					return false;
				}
			} else {
				EntityInfo dataInfo = new EntityInfo();
				dataInfo.setDataModelURI(dataModelURI);
				dataInfo.setInstanceID(instanceID);
				dataInfo.setDataNameURI(new DataNameURI(elementURI));
				dataInfo.isDefined(false);
				dataInfo.isSkipped(false);

				if(!this.dbGateway.addData(dataInfo, relation)) {
					return false;
				}

				for (AddDataMessage addDataMessage : attributes) {
					DataInfo att = new DataInfo();
					att.setDataModelURI(dataModelURI);
					att.setInstanceID(instanceID);
					att.setDataNameURI(new DataNameURI(elementURI, addDataMessage.getDataName()));
					att.setDataType(addDataMessage.getDataType());
					att.isDefined(false);
					att.isSkipped(false);
					att.isKey(addDataMessage.isKey());
					if(!this.dbGateway.addData(att, null)) {
						return false;
					}
				}
			}
			return true;
		} catch (IOException e) {
			log.error("Could not connect to the data repository");
		}
		return false;
	}
}
