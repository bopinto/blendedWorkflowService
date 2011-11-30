package pt.utl.ist.goalengine.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.exceptions.GoalHierarchyConsistencyException;
import pt.utl.ist.goalengine.EngineClientInterface;
import pt.utl.ist.goalengine.GoalEngine;

public class GoalTree {
	
	protected static String STATE_DEACTIVATED = "Deactivated";
	protected static String STATE_RUNNING = "Running";
	
	private String treeID;
	private Goal rootGoal;
	private HashMap<String, Goal> goals = new HashMap<String, Goal>(); //key = goal id 
	private String state;
	
	private String instanceID = null;
	
	// the data model the goal tree refers to
	private String dataModelURI;
	
	public GoalTree(String id, Goal root, String dataModelURI) {
		this.treeID = id;
		this.rootGoal = root;
		this.dataModelURI = dataModelURI;
		this.state = STATE_DEACTIVATED;
		registerGoalTree(root);
	}
	
	/********* GETTERS **********/
	public String getTreeID() { return this.treeID; }
	public Goal getRootGoal() { return this.rootGoal; }
	public Goal getGoal(String id) { return this.goals.get(id); }
	public String getTreeState() { return this.state; }
	
	public ArrayList<GoalWorkItem> getActiveGoals() {
		ArrayList<GoalWorkItem> activeGoals = new ArrayList<GoalWorkItem>();
		
		Collection<Goal> goalCollection = this.goals.values();
		ArrayList<Goal> goals = new ArrayList<Goal>(goalCollection);
		
		for (Goal goal : goals) {
			if(goal.getState().equals(GoalState.ACTIVATED)) {
				GoalWorkItem gwir = new GoalWorkItem(new CaseURI(this.treeID), new CaseInstanceID(this.instanceID), new DataModelURI(this.dataModelURI),
						goal.getName(), goal.getID(), goal.getDefinition(), 
						goal.isMandatory());
				activeGoals.add(gwir);
			}
		}
		
		return activeGoals;
	}
	
	public String getInstanceID() { return this.instanceID; }
	public String getIDForDisplay() { return this.instanceID + ":" + this.treeID; }
	
	/****************************/
	
	/****** SETTERS ********/
	public void setInstanceID(int id) {	this.instanceID = "" + id; }
	
	/***********************/
	
	public void run() {
		this.state = STATE_RUNNING;
	}
	
	/**
	 * Updates the given goal to "achieved" state and re-evaluates the tree, looking for new goals
	 * @param goalID the goal id.
	 */
	public void updateGoal(String goalID) {
		Goal goal = this.getGoal(goalID);
		if(goal == null) {
			return;
		}
		
		goal.executed();
		
		this.evaluateTree(rootGoal);
		
	}
	
	public void skipGoal(String goalID) {
		Goal goal = this.getGoal(goalID);
		if(goal == null) {
			return;
		}
		
		goal.skip();
		
		this.evaluateTree(rootGoal);
	}
	
	public void notifyAutoSkippedGoal(Goal g) {
		EngineClientInterface.get().skipGoal(this.getInstanceID() + ":" + g.getName(), g.isMandatory());
	}
	
	public void notifyAutoExecutedGoal(Goal g) {
		EngineClientInterface.get().executeGoal(this.getInstanceID() + ":" + g.getName(), g.isMandatory());
	}
	
	public String addSubGoal(Goal newGoal, Goal parentGoal) throws GoalHierarchyConsistencyException {
		String newGoalID = newGoal.getName().replace(' ', '_') + "_" + this.goals.size();
		newGoal.setID(newGoalID);
		
		if(newGoal.isMandatory() && !parentGoal.isMandatory()) {
			throw new GoalHierarchyConsistencyException();
		}
		
		this.goals.put(newGoalID, newGoal);
		parentGoal.addSubGoal(newGoal);
		
		this.evaluateTree(rootGoal);
		
		//GoalEngine.get().treeUpdate(this.instanceID);
		
		return newGoalID;
	}
	
	/**
	 * Evaluates the tree. If there are new goals available, the goal engine is notified. 
	 */
	public void evaluateTree(Goal g) { //TODO test this
		//go recursively until hit the bottom.
		for (Goal subGoal : g.getSubGoals()) {
			evaluateTree(subGoal);
		}
		if(g.getState() == GoalState.EXECUTED ||
				g.getState() == GoalState.SKIPPED) {
			return;
		} else if(g.getState() == GoalState.DEACTIVATED ||
				g.getState() == GoalState.ACTIVATED) { // just in case you add a mandatory sub goal
			if(g.getSubGoals().size() > 0) {
				boolean allFinished = true;
				for (Goal child : g.getSubGoals()) {
					if((child.getState() == GoalState.ACTIVATED 
							|| child.getState() == GoalState.DEACTIVATED) 
							&& child.isMandatory()) {
						allFinished = false;
						break;
					}
				}
				if(allFinished) {
					g.activate();
				} else {
					g.deactivate();
				}
			} else {
				g.activate();
			}
		}
	}
	
	protected void registerGoalTree(Goal root) {
		for (Goal g : root.getSubGoals()) {
			registerGoalTree(g);
		}
		this.goals.put(root.getID(), root);
		root.setParentTree(this);
	}
}
