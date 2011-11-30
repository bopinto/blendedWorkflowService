package pt.utl.ist.goalengine.elements;

import java.util.ArrayList;

public class Goal {

	private GoalTree parent;
	
	private GoalState state;
	private String ID;
	private String name;
	private String definition;
	private boolean mandatory;
	
	private ArrayList<Goal> subGoals = new ArrayList<Goal>();
	
	public Goal(GoalTree parentTree, String id, String name, String definition, boolean mandatory) {
		this.parent = parentTree;
		this.ID = id;
		this.name = name;
		this.mandatory = mandatory;
		this.definition = definition;
		this.state = GoalState.DEACTIVATED;
	}
	
	/********** GETTERS ***********/
	public String getID() { return this.ID;	}
	public String getName() { return this.name; }
	public boolean isMandatory() { return this.mandatory; }
	public GoalState getState() { return this.state; }
	public String getDefinition() { return this.definition; }
	public ArrayList<Goal> getSubGoals() { return this.subGoals; }
	/*****************************/
	
	public void setID(String id) { this.ID = id; }
	public void setParentTree(GoalTree parentTree) { this.parent = parentTree; }
	
	public void activate() {
		this.state = GoalState.ACTIVATED;
	}
	
	public void deactivate() {
		this.state = GoalState.DEACTIVATED;
	}
	
	public void skip() {
		if(this.state != GoalState.EXECUTED) {
			this.state = GoalState.SKIPPED;
			parent.notifyAutoSkippedGoal(this);
		}
		for (Goal subGoal : this.subGoals) {
			subGoal.skip();
		}
	}
	
	public void executed() {
		this.state = GoalState.EXECUTED;
		parent.notifyAutoExecutedGoal(this);
		
		for (Goal subGoal : this.subGoals) {
			if(subGoal.state == GoalState.ACTIVATED) {
				subGoal.executed();
			}
		}
	}
	
	public void addSubGoal(Goal subGoal) {
		this.subGoals.add(subGoal);
	}
}
