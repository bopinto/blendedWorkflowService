package pt.utl.ist.goalengine;

import pt.utl.ist.bw.bwgoalmanager.BWGoalManager;

/**
 * Handles the events where the engine is the client (emits events)
 * @author bernardoopinto
 *
 */
public class EngineClientInterface {
	
	private static EngineClientInterface instance = null;
	
	private EngineClientInterface() {}
	
	public static EngineClientInterface get() {
		if(instance == null) {
			instance = new EngineClientInterface();
		}
		return instance;
	}
	
	public void notifyOfActiveGoals(String instanceID) {
		// notify the BWGoal Manager
		BWGoalManager.get().notifyOfActiveGoals(instanceID);
	}
	
	public void skipGoal(String goalURI, boolean isMandatory) {
		BWGoalManager.get().skipGoalEngineRequest(goalURI, isMandatory);
	}
	
	public void executeGoal(String goalURI, boolean isMandatory) {
		BWGoalManager.get().executeGoalEngineRequest(goalURI, isMandatory);	
	}

}
