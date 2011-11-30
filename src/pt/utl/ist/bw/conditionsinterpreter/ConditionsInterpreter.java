package pt.utl.ist.bw.conditionsinterpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import pt.utl.ist.bw.bwactivitymanager.BWActivityManager;
import pt.utl.ist.bw.bwgoalmanager.BWGoalManager;
import pt.utl.ist.bw.conditionsinterpreter.conditioncompiler.ConditionFactory;
import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.TripleStateBool;
import pt.utl.ist.bw.elements.CaseIDForDisplay;
import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;
import pt.utl.ist.bw.elements.TaskURI;
import pt.utl.ist.bw.exceptions.UnregisteredConditionException;
import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.EntityInfo;

public class ConditionsInterpreter {
	
	private HashMap<String, ElementSet> dataAssociations = new HashMap<String, ElementSet>(); // HashMap<DataNameURI, Set<idActivity/idGoal>>
	private DataEventHandlerClient dataEventHandler;
	private ArrayList<CaseAssociation> activeCases = new ArrayList<CaseAssociation>();
	
	private static Logger _log = Logger.getLogger(ConditionsInterpreter.class);
	private static ConditionsInterpreter instance = null;
	
	private ConditionsInterpreter() {
		this.dataEventHandler = new DataEventHandlerClient("http://localhost:8080/dataRepository/bw"); //FIXME hard coded
	}
	
	public static ConditionsInterpreter get() {
		if(instance == null) {
			instance = new ConditionsInterpreter();
		}
		return instance;
	}
	
	public void loadNewCase(String dataModelURI, String caseURI, boolean isActivity) {
		//TODO stub method
	}
	
	public boolean registerNewCase(DataModelURI dataModelURI, CaseURI caseURI, CaseInstanceID caseInstanceID, boolean isActivity) { //TODO test
		CaseAssociation currentCase = null;
		if(isActivity) {
			//registering with the activity case
			for (CaseAssociation caseAssoc : this.activeCases) {
				if(caseAssoc.getActivityInstanceID() == null) {
					caseAssoc.setActivityURI(caseURI);
					caseAssoc.setActivityInstanceID(caseInstanceID);
					currentCase = caseAssoc;
					break;
				}
			}
			if(currentCase == null) {
				CaseAssociation newCase = new CaseAssociation(dataModelURI);
				newCase.setActivityURI(caseURI);
				newCase.setActivityInstanceID(caseInstanceID);
				this.activeCases.add(newCase);
			}
		} else {
			for (CaseAssociation caseAssoc : this.activeCases) {
				// look for a case with no goal case
				if(caseAssoc.getGoalInstanceID() == null) {
					caseAssoc.setGoalSpecURI(caseURI);
					caseAssoc.setGoalInstanceID(caseInstanceID);
					currentCase = caseAssoc;
					break;
				}
			}
			if(currentCase == null) {
				CaseAssociation newCase = new CaseAssociation(dataModelURI);
				newCase.setActivityURI(caseURI);
				newCase.setGoalInstanceID(caseInstanceID);
				this.activeCases.add(newCase);
			}
		}
		
		if(currentCase != null && currentCase.getActivityInstanceID() != null && currentCase.getGoalInstanceID() != null) {
			try {
				DataModelInstanceID dataModelInstanceID = dataEventHandler.createNewModelInstance(currentCase.getDataModelURI(), currentCase.getActivitySpecURI(), currentCase.getActivityInstanceID(),
						currentCase.getGoalSpecURI(), currentCase.getGoalInstanceID());
				currentCase.setDataModelInstanceID(dataModelInstanceID);
				// now I have all the instance numbers, I can create the condition instances
				CaseIDForDisplay goalCaseIDForDisplay = new CaseIDForDisplay(currentCase.getGoalInstanceID(), currentCase.getGoalSpecURI());
				pt.utl.ist.bw.bwgoalmanager.ConditionsDB.get().createNewInstance(goalCaseIDForDisplay, dataModelInstanceID);
				CaseIDForDisplay activityCaseIDForDisplay = new CaseIDForDisplay(currentCase.getActivityInstanceID(), currentCase.getActivitySpecURI());
				pt.utl.ist.bw.bwactivitymanager.ConditionsDB.get().createNewPreConditionInstance(activityCaseIDForDisplay, dataModelInstanceID);
				pt.utl.ist.bw.bwactivitymanager.ConditionsDB.get().createNewPostConditionInstance(activityCaseIDForDisplay, dataModelInstanceID);
			} catch(UnregisteredConditionException uce) {
				_log.error("Given condition is not registered in the system.");
				return false;
			}
			catch (IOException e) {
				_log.error("Could not initialize the data model.");
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
	
	public TripleStateBool evaluateCondition(Condition condition) {
		return ConditionEvaluator.evaluateCondition(condition);
	}
	
	/**
	 * Updates the information about a data element.
	 * @param dataInfo the data information structure
	 * @param caseID the instance id of the case (activity or goal case)
	 */
	public void updateDataInfo(DataInfo dataInfo, CaseInstanceID caseID, boolean isActivity) {
		try {
			CaseAssociation currentCase = null;
			if(isActivity) {
				for (CaseAssociation caseAssoc : this.activeCases) {
					if(caseID.equals(caseAssoc.getActivityInstanceID())) {
						currentCase = caseAssoc;
					}
				}
			} else if(!isActivity) {
				for (CaseAssociation caseAssoc : this.activeCases) {
					if(caseID.equals(caseAssoc.getGoalInstanceID())) {
						currentCase = caseAssoc;
					}
				}
			}
			if(currentCase == null) {
				_log.error("The given case is not registered");
				return;
			}
			dataInfo.setInstanceID(currentCase.getDataModelInstanceID());
			ArrayList<DataInfo> data = dataEventHandler.getElementDataInfo(dataInfo.getDataModelURI(), dataInfo.getDataNameURI(), dataInfo.getInstanceID());
			if(data.size() > 1) {
				// TODO what to do?!
			} else {
				dataInfo = data.get(0);
			}
		} catch (IOException e) {
			_log.error("Could not get the data information");
		}
	}
	
	/**
	 * Gets the data information about a data element.
	 * @param dataName the data name ([Element].[Attribute])
	 * @param dataModelID the data model instance id
	 * @return the information about the data element
	 */
	public ArrayList<DataInfo> getDataInfo(DataNameURI dataName, DataModelURI dataModelURI, 
			DataModelInstanceID dataModelID) {
		try {
			return dataEventHandler.getElementDataInfo(dataModelURI, dataName, dataModelID);
		} catch (IOException e) {
			_log.error("Could not get the data information");
		}
		return null;
	}
	
	public EntityInfo getEntityInfo(DataNameURI dataName, DataModelURI dataModelURI, DataModelInstanceID dataModelID) {
		try {
			return dataEventHandler.getEntityInfo(dataModelURI, dataName, dataModelID);
		} catch (IOException e) {
			_log.error("Could not get the data information");
		}
		return null;
	}
	
	public String getParameterMapping(DataModelURI dataModelURI, String parameter) {
		try {
			return dataEventHandler.getParameterMapping(dataModelURI, parameter);
		} catch (IOException ioe) {
			_log.error("Could not connect to the data repository");
		}
		return null;
	}
	
	public boolean skipData(Condition condition) {
		ArrayList<DataInfo> conditionData = condition.getData();
		
		for (DataInfo dataInfo : conditionData) {
			if(!this.skipData(dataInfo)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean skipData(DataInfo dataInfo) {
		try {
			if(!dataEventHandler.skipData(dataInfo.getDataModelURI().toString(), dataInfo.getInstanceID().toString(), dataInfo.getDataNameURI().toString())) {
				return false;
			}
		} catch (IOException e) {
			_log.error("Could not skip the given data");
		}
		return true;
	}
	
	public ArrayList<DataInfo> getDataFromCondition(Condition condition) {
		ArrayList<DataInfo> conditionData = condition.getData();
		return conditionData;
	}
	
	public ArrayList<DataInfo> getKeyDataFromCondition(Condition condition) {
		ArrayList<DataInfo> conditionData = condition.getKeyData();
		return conditionData;
	}
	
	/**
	 * registers a condition in the engine 
	 * @param taskURI the task uri (must be caseInstanceID:caseURI:taskName)
	 * @param condition the condition
	 * @param type the type of condition: must be 'pre', 'post' or 'goal'
	 * @return true if successful.
	 */
	public boolean registerCondition(TaskURI taskURI, Condition condition, String type) {
		if(type == null) {
			return false;
		}
		if(!"pre".equals(type) && !"post".equals(type) && !"goal".equals(type)) {
			return false;
		}
		
		ArrayList<DataInfo> conditionData = condition.getData();
		
		for (DataInfo dataInfo : conditionData) {
			if(dataAssociations.containsKey(dataInfo.getDataNameURI().toString())) {
				if("pre".equals(type)) {
					dataAssociations.get(dataInfo.getDataNameURI().toString()).preconditions.add(taskURI);
				} else if("post".equals(type)) {
					dataAssociations.get(dataInfo.getDataNameURI().toString()).postconditions.add(taskURI);
				} else if("goal".equals(type)) {
					dataAssociations.get(dataInfo.getDataNameURI().toString()).goals.add(taskURI);
				}
			} else {
				ElementSet tmp = new ElementSet();
				if("pre".equals(type)) {
					tmp.preconditions.add(taskURI);
				} else if("post".equals(type)) {
					tmp.postconditions.add(taskURI);
				} else if("goal".equals(type)) {
					tmp.goals.add(taskURI);
				}
				dataAssociations.put(dataInfo.getDataNameURI().toString(), tmp);
			}
		}
		
		return true;
	}
	
	public DataModelInstanceID getDataInstanceID(CaseInstanceID caseID, boolean isActivity) {
		CaseAssociation currentCase = null;
		if(!isActivity) {
			for (CaseAssociation caseAssoc : this.activeCases) {
				if(caseID.equals(caseAssoc.getGoalInstanceID())) {
					currentCase = caseAssoc;
					break;
				}
			}	
		} else if(isActivity) {
			for (CaseAssociation caseAssoc : this.activeCases) {
				if(caseID.equals(caseAssoc.getActivityInstanceID())) {
					currentCase = caseAssoc;
					break;
				}
			}
		}
		if(currentCase == null) {
			_log.error("The given case is not registered");
			return null;
		}
		
		return currentCase.getDataModelInstanceID();
	}
	
	public DataModelURI getDataModelURI(CaseInstanceID caseID, boolean isActivity) {
		CaseAssociation currentCase = null;
		if(!isActivity) {
			for (CaseAssociation caseAssoc : this.activeCases) {
				if(caseID.equals(caseAssoc.getGoalInstanceID())) {
					currentCase = caseAssoc;
				}
			}	
		} else if(isActivity) {
			for (CaseAssociation caseAssoc : this.activeCases) {
				if(caseID.equals(caseAssoc.getActivityInstanceID())) {
					currentCase = caseAssoc;
				}
			}
		}
		if(currentCase == null) {
			_log.error("The given case is not registered");
			return null;
		}
		
		return currentCase.getDataModelURI();
	}
	
	public String getDataModel(DataModelURI dataModelURI, DataModelInstanceID dataModelID) {
		if(dataModelID == null || dataModelURI == null) {
			return null;
		}
		try {
			return dataEventHandler.getDataModel(dataModelURI, dataModelID);
		} catch (IOException e) {
			_log.error("Could not get the data model");
			return null;
		}
	}
	
	public boolean notifyOfDataUpdate(String dataModelURI, String dataModelInstanceID, String elementURI) {
		ElementSet activitiesAndGoals = this.dataAssociations.get(elementURI);
		if(activitiesAndGoals == null) {
			return false;
		}
		
		CaseAssociation caseAssoc = searchCaseAssociation(dataModelURI, dataModelInstanceID);
		
		if(caseAssoc == null) { return false; }
		
		for(TaskURI activityURI : activitiesAndGoals.postconditions) {
			BWActivityManager.get().reevaluatePostCondition(activityURI);
		}
		for (TaskURI goalURI : activitiesAndGoals.goals) {
			BWGoalManager.get().reevaluateGoalDefinition(goalURI);
		}
		return true;
	}
	
	public boolean validateCondition(String condition) {
		return ConditionFactory.isConditionValid(condition);
	}
	
	protected CaseAssociation searchCaseAssociation(String dataModelURI, String dataModelInstanceID) {
		if(dataModelURI == null || dataModelInstanceID == null) {
			return null;
		}
		
		for (CaseAssociation caseAssociation : this.activeCases) {
			if(caseAssociation.getDataModelURI().toString().equals(dataModelURI) && 
					caseAssociation.getDataModelInstanceID().toString().equals(dataModelInstanceID)) {
				return caseAssociation;
			}
		}
		return null;
	}
}
