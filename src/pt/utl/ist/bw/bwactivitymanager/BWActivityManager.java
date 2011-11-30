package pt.utl.ist.bw.bwactivitymanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.YParametersSchema;

import pt.utl.ist.bw.conditionsinterpreter.ConditionsInterpreter;
import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.TripleStateBool;
import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.TaskID;
import pt.utl.ist.bw.elements.TaskURI;
import pt.utl.ist.bw.exceptions.ImpossibleToSkipTaskException;
import pt.utl.ist.bw.exceptions.UnregisteredConditionException;
import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.GroupDataInfo;
import pt.utl.ist.bw.messages.TaskInfo;
import pt.utl.ist.bw.utils.StringUtils;
import pt.utl.ist.bw.worklistmanager.WorklistManager;


/**
 * The service that handles the activity spec from Blended Workflow
 * @author bernardoopinto
 *
 */

public class BWActivityManager {
	
	// work items that are enabled but not checked out
	private ArrayList<WorkItemRecord> enabledWorkItems = new ArrayList<WorkItemRecord>();
	
	// checked out work items
	private ArrayList<WorkItemRecord> activeWorkItems = new ArrayList<WorkItemRecord>();
	
	// suspended work items (a pre-activity case was launched)
	private ArrayList<WorkItemRecord> suspendedWorkItems = new ArrayList<WorkItemRecord>();
	
	private ArrayList<String> runningCases = new ArrayList<String>();

	private Logger log = null;
	private static BWActivityManager instance = null;
	
	private BWActivityManager() {
		log = Logger.getLogger(BWActivityManager.class);
	}
	
	public static BWActivityManager get() {
		if(instance == null) {
			instance = new BWActivityManager();
		}
		return instance;
	}
	
	public String launchActivityCase(String caseUUID, String caseURI, String dataModelURI) {
		String caseInstanceID = ActivityEventRouter.getInstance().launchYAWLcase(caseUUID, true);
		if(caseInstanceID != null) {
			this.runningCases.add(caseInstanceID);
			ConditionsInterpreter.get().registerNewCase(new DataModelURI(dataModelURI), new CaseURI(caseURI), 
					new CaseInstanceID(caseInstanceID), true);
		} else {
			return null;
		}
		
		return caseInstanceID;
	}
	
	public void activateTasks(String caseInstanceID) {
		ActivityEventRouter.getInstance().notifyActiveTasks(caseInstanceID);
	}
	
	/**
	 * Unloads a YAWL specification.
	 * @param specID the specification UUID.
	 * @return true if the specification is successfully unloaded and false otherwise.
	 */
	public boolean unloadYAWLSpecification(String specID) {
		return ActivityEventRouter.getInstance().unloadSpecification(specID, true);
	}
	
	public boolean unloadConditionsSpecification(String caseURI) {
		return ConditionsDB.get().removeSpecification(new CaseURI(caseURI));
	}
	
	/**
	 * Receives a specification and sends it to the event router, to load it 
	 * @param specID the UUID of the specification.
	 * @param specInString the specification (in a string)
	 * @return true if the specification is successfully received.
	 */
	public boolean receiveActivitySpec(String specID, String specInString) {
		return ActivityEventRouter.getInstance().loadSpecification(specID, specInString, true);
	}
	
	/**
	 * Receives the Blended Workflow specification and stores the conditions specification
	 * @param caseURI the specification URI
	 * @param specInString the Blended Workflow specification (in a string) 
	 * @return
	 */
	public boolean receiveConditionsSpec(CaseURI caseURI, String specInString) {
		Document doc = StringUtils.stringToDoc(specInString);
		if(doc != null) {
			Element root = doc.getRootElement();
			Namespace bwNs = root.getNamespace();

			// get the conditions spec
			Element conditionsSpec = root.getChild("conditionsSpec", bwNs);
			
			// get the data model URI
			DataModelURI dataModelURI = new DataModelURI(root.getChildText("dataModelURI", bwNs));

			ArrayList<TaskID> activityList = ConditionsDB.get().insertNewSpec(caseURI);
			// for each activity, get the precondition and the postcondition
			for(int i=0; i < conditionsSpec.getChildren().size(); i++) {
				Element activityCond = (Element) conditionsSpec.getChildren().get(i);
				if(activityCond.getName().equals("activityCond")) {
					String taskName = activityCond.getChildText("activityID", bwNs);
					String taskPrecond = activityCond.getChildText("precondition", bwNs);
					String taskPostcond = activityCond.getChildText("postcondition", bwNs);
					if(!ConditionsDB.get().addPrecondition(caseURI, taskName, taskPrecond, dataModelURI) ||
							!ConditionsDB.get().addPostcondition(caseURI, taskName, taskPostcond, dataModelURI)) {
						ConditionsDB.get().removePrecondition(caseURI, taskName);
						ConditionsDB.get().removePostcondition(caseURI, taskName);
						return false;
					} else {
						activityList.add(ConditionsDB.get().getTaskID(caseURI, taskName));
					}
				}
			}

			return true;
		}
		return false;
	}
	
	public void registerEnabledTask(WorkItemRecord wir) { 
		this.enabledWorkItems.add(wir);
		
		try {
			TripleStateBool result = evaluatePostCondition(wir);

			if(result == TripleStateBool.TRUE) { // if it's true
				// get the input output data
				checkInAndOut(wir);
				this.enabledWorkItems.remove(wir);
			} else {
				WorklistManager.get().registerActiveTask(wir.getIDForDisplay());
			}
		} catch (UnregisteredConditionException e) {
			log.error("The given work item does not have a registered condition");
		}
	}

	public boolean receiveTaskExecutionResult(TaskInfo taskInfo) {
		WorkItemRecord wir = null;
		for (WorkItemRecord wirIt : this.activeWorkItems) {
			if(wirIt.getIDForDisplay().equals(taskInfo.getTaskName())) {
				wir = wirIt;
				break;
			}
		}
		if(wir == null) { // try the suspended work items
			for (WorkItemRecord wirIt : this.suspendedWorkItems) {
				if(wirIt.getIDForDisplay().equals(taskInfo.getTaskName())) {
					wir = wirIt;
					break;
				}
			}
			
			if(wir == null) {
				log.error("Work item record does not exist");
				return false;
			} else {
				TaskInfo newTaskInfo = receivePreActivityExecutionResult(wir, taskInfo);
				this.suspendedWorkItems.remove(wir);
				this.activeWorkItems.add(wir);
				// send back to the interface
				WorklistManager.get().continueExecutingTask(newTaskInfo);
			}
		} else {
			// send to the Event manager
			if(!ActivityEventRouter.getInstance().checkInWorkItem(wir, taskInfo)) {
				return false;
			}
			this.activeWorkItems.remove(wir);
		}
		return true;
	}
	
	// put the output data from the task info into the input data of the activity
	public TaskInfo receivePreActivityExecutionResult(WorkItemRecord wir, TaskInfo preActivityTaskInfo) {
		TaskInfo taskInfo = getInfoToExecuteTask(wir);
		
		for(GroupDataInfo inputGroupData : taskInfo.getInputData()) {
			for (DataInfo inputInfo : inputGroupData.getFields()) {
				String mapping = ConditionsInterpreter.get().getParameterMapping(inputInfo.getDataModelURI(),
						inputInfo.getDataNameURI().toString());
				if(mapping != null && !"".equals(mapping)) {
					String[] map = mapping.split("\\.");
					DataInfo outputData = preActivityTaskInfo.getOutputData(map[0], map[1]);
					if(outputData != null) {
						inputInfo.setValue(outputData.getValue());
					}
				}
			}
		}
		return taskInfo;
	}
	
	/**
	 * Skips a YAWL activity 
	 * @param taskName the taskURI (as it appears in the work list)
	 * @throws ImpossibleToSkipTaskException if you cannot skip the task (e.g. it is a pre-activity)
	 */
	public void skipTask(String taskName) {

		// if it is in the enabled work items list
		WorkItemRecord wirToSkip = null;
		for (WorkItemRecord wir : this.enabledWorkItems) {
			if(wir.getIDForDisplay().equals(taskName)) {
				wirToSkip = wir;
				break;
			}
		}
		if(wirToSkip == null) {
			return;
		}
		// skip the work item (send request to event manager)
		this.enabledWorkItems.remove(wirToSkip);
		
		// FIXME for now this command does not work
		// ActivityEventRouter.getInstance().skipWorkItem(wirToSkip);
		
		checkInAndOut(wirToSkip);
		
		try {
			// send info to the ConditionEngine
			DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(new CaseInstanceID(wirToSkip.getCaseID()), true);
			// get the task post condition
			Condition postCondition;
			postCondition = ConditionsDB.get().getPostcondition(new CaseURI(wirToSkip.getSpecURI()), wirToSkip.getTaskID(), dataModelID);
			ConditionsInterpreter.get().skipData(postCondition);
		} catch (UnregisteredConditionException e) {
			log.error("The given work item does not have a registered post condition");
		}
	}
	
	/**
	 * Gets the information (input and output data) to execute a task.
	 * @param taskName the task URI.
	 * @return the task information.
	 */
	public TaskInfo startEnabledTask(String taskName) {
		WorkItemRecord wir = null;
		for (WorkItemRecord wirIt : this.enabledWorkItems) {
			if(wirIt.getIDForDisplay().equals(taskName)) {
				wir = wirIt;
				break;
			}
		}
		
		if(wir == null) {
			log.error("The work-item is not registered in the system");
			return null;
		}
		
		
		// check-out the work item
		WorkItemRecord newWir = ActivityEventRouter.getInstance().checkOutWorkItem(wir);
		if(newWir == null) {
			log.error("Could not check out the work item");
			return null;
		}
		// get the pre-condition
		CaseInstanceID caseInstanceID = new CaseInstanceID(wir.getCaseID());
		DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(caseInstanceID, true);
		
		Condition precondition;
		try {
			precondition = ConditionsDB.get().getPrecondition(new CaseURI(wir.getSpecURI()), wir.getTaskID(), dataModelID);
		} catch (UnregisteredConditionException e) {
			log.error("This condition is not registered in the system");
			return null;
		}
		// evaluate pre-condition
		TripleStateBool result = ConditionsInterpreter.get().evaluateCondition(precondition);
		// if it's true
		if(result == TripleStateBool.TRUE) {
			TaskInfo taskInfo = getInfoToExecuteTask(newWir);
			
			this.enabledWorkItems.remove(wir);
			this.activeWorkItems.add(newWir);
			
			return taskInfo;
			
		} else if(result == TripleStateBool.SKIPPED) { //if it's skipped
			// get the data from the condition
			ArrayList<DataInfo> data = ConditionsInterpreter.get().getKeyDataFromCondition(precondition);
			// get the skipped data
			ArrayList<DataInfo> skippedData = new ArrayList<DataInfo>();
			for (DataInfo dataInfo : data) {
				if(dataInfo.isSkipped()) {
					skippedData.add(dataInfo);
				}
			}
			// generate the pre-activity
			this.suspendedWorkItems.add(newWir);
			return buildPreActivityTaskInfo(newWir, skippedData);
		}
	
		return null;
	}
	
	public TaskInfo getInfoToExecuteTask(WorkItemRecord wir) {
		TaskInfo taskInfo = new TaskInfo(TaskInfo.TASK_TYPE_ACTIVITY, wir.getIDForDisplay());
		TaskInformation workItemInformation = ActivityEventRouter.getInstance().getTaskInformation(wir);
		CaseInstanceID caseInstanceID = new CaseInstanceID(wir.getCaseID());
		DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(caseInstanceID, true);
		DataModelURI dataModelURI = ConditionsInterpreter.get().getDataModelURI(caseInstanceID, true);
		this.getInputDataFromWorkItem(wir, workItemInformation, dataModelURI, dataModelID, taskInfo);
		this.getOutputDataFromWorkItem(wir, workItemInformation, dataModelURI, dataModelID, taskInfo);
		
		return taskInfo;
	}
	
	public void reevaluatePreCondition(TaskURI activityURI) { //FIXME is this necessary?
		// case it is a normal task
		
	}
	
	public void reevaluatePostCondition(TaskURI activityURI) {
		//case it is a normal task
		String taskIDForDisplay = activityURI.getTaskIDForDisplay();
		
		WorkItemRecord wir = null;
		for (WorkItemRecord wirIt : this.enabledWorkItems) {
			if(wirIt.getID().equals(taskIDForDisplay)) {
				wir = wirIt;
				break;
			}
		}
		if(wir == null) { return; }
		
		try {
			TripleStateBool result = evaluatePostCondition(wir);
			
			if(result == TripleStateBool.TRUE) {
				checkInAndOut(wir);
				this.enabledWorkItems.remove(wir);
				WorklistManager.get().removeTaskFromInterface(taskIDForDisplay);
			}
		} catch(UnregisteredConditionException e) {
			log.error("Given work item record is not registerd in the system.");
			return;
		}
	}
	
	//////// INTERNAL METHODS ////////
	
	// usefull when the post condition is true
	private void checkInAndOut(WorkItemRecord wir) {
		//check-out wir
		WorkItemRecord newWir = ActivityEventRouter.getInstance().checkOutWorkItem(wir);
		if(newWir == null){
			log.error("Could not check-out the work item");
			return;
		}

		TaskInfo taskInfo = getInfoToExecuteTask(newWir);
		
		if(!ActivityEventRouter.getInstance().checkInWorkItem(wir, taskInfo)) {
			log.error("Could not check in work item");
			return;
		}
		WorklistManager.get().removeTaskFromInterface(wir.getIDForDisplay());
	}
	
	private TripleStateBool evaluatePostCondition(WorkItemRecord wir)
	throws UnregisteredConditionException {
		DataModelInstanceID dataModelID = ConditionsInterpreter.get().getDataInstanceID(new CaseInstanceID(wir.getCaseID()), true);
		// get the task post condition
		Condition postCondition = ConditionsDB.get().getPostcondition(new CaseURI(wir.getSpecURI()), wir.getTaskID(), dataModelID);

		//evaluate the post condition
		TripleStateBool result = ConditionsInterpreter.get().evaluateCondition(postCondition);
		return result;
	}
	
	private void getInputDataFromWorkItem(WorkItemRecord wir, TaskInformation wirInfo, DataModelURI dataModelURI, 
			DataModelInstanceID dataModelID, TaskInfo taskInfo) {
		//FIXME what if it is complex data?
	    org.jdom.Element inputData = wir.getDataList();
	    YParametersSchema params;
	    if (wirInfo == null) {
	      return;
	    }
	    params = wirInfo.getParamSchema();
	    List<YParameter> _input = params.getInputParams();
	    YParameter parameter = null;
	    
	    for (int i = 0; i < _input.size(); i++) {
	      parameter = (YParameter) _input.get(i);

	      String name = parameter.getName(); // name

	      String value = parameter.getInitialValue(); // value
	      if (inputData != null) {
	        Element inputChild = (Element) inputData.getChild(name); 
	    	if(inputChild.getChildren().size() > 0) {
	    		// complex type
	    		for (Object child : inputChild.getChildren()) {
	    			Element childEl = (Element) child;
	    			taskInfo.addInputData(name, childEl.getName(), dataModelURI, 
	    					dataModelID, (childEl.getText().equals("true") || 
	    							childEl.getText().equals("false") ? "BOOLEAN" : "STRING"), null, childEl.getText());
	    		}
	    	} else {
	    		//simple type
	    		String type = parameter.getDataTypeName(); // type
	    		value = inputChild.getText();
	    		taskInfo.addInputData(null, name, dataModelURI, dataModelID, type, null, value);
	    	}
	      }
	    }
	}
	
	private void getOutputDataFromWorkItem(WorkItemRecord wir, TaskInformation wirInfo, 
			DataModelURI dataModelURI, DataModelInstanceID dataModelID, TaskInfo taskInfo) { 
		YParametersSchema params;
	    if (wirInfo == null) {
	      return;
	    }
	    params = wirInfo.getParamSchema();
	    List<YParameter> _output = params.getOutputParams();
	    YParameter parameter = null;     
	    
	    YSpecificationID specID = new YSpecificationID(wir);
	    Element specSchema = ActivityEventRouter.getInstance().getSpecDataSchema(specID);
	    	
	    for (int i = 0; i < _output.size(); i++) {
	      parameter = (YParameter) _output.get(i);
	      String value = parameter.getInitialValue();
	      addOutputDataToTaskInfo(taskInfo, parameter, specSchema, dataModelURI, dataModelID, value);
	    }
	}
	
	protected void addOutputDataToTaskInfo(TaskInfo taskInfo, YParameter parameter, 
			Element schema, DataModelURI dataModelURI, DataModelInstanceID dataModelID, String value) {
		// if schema is empty
		if(schema == null || schema.getChildren().size() == 0) {
			// simple type (w/out group)
			taskInfo.addOutputData(null, parameter.getName(), dataModelURI, dataModelID, parameter.getDataTypeName(), null, value);
		} else {
			// see if the parameter is in "xs:" namespace
			if(parameter.getDataTypePrefix().equals("xs") || parameter.getDataTypeName().contains("string")
					|| parameter.getDataTypeName().contains("boolean") || parameter.getDataTypeName().contains("number")) {
				taskInfo.addOutputData(null, parameter.getName(), dataModelURI, dataModelID, parameter.getDataTypeName(), null, value);
			} else {
				// if not, see if it is simple type or complex type
				Element paramType = getParameterTypeWithName(schema, parameter.getDataTypeName());
				if(paramType == null) {
					return;
				}
				Namespace paramns = paramType.getNamespace(); 
				if(paramType.getName().equals("simpleType")) {
					//if simple type, see the base
					String paramTypeString = paramType.getChild("restriction", paramns).getAttributeValue("base", paramns);
					taskInfo.addOutputData(null, parameter.getName(), dataModelURI, dataModelID, paramTypeString, null, value);
				} else {
					//if complex type, go to each child and
					for (Object child : paramType.getChild("sequence", paramns).getChildren()) {
						Element childEl = (Element) child;
						String childType = childEl.getAttributeValue("type");
						//see if child is in "xs:" namespace
						if(childType.startsWith("xs:")) {   // if is, add
							taskInfo.addOutputData(parameter.getName(), childEl.getAttributeValue("name"), dataModelURI, dataModelID, childType, null, value);
						} else {
							// if not, search for the type in the schema
							Element childTypeEl = getParameterTypeWithName(schema, childType);
							String childTypeElString = childTypeEl.getChild("restriction", paramns).getAttributeValue("base");  //FIXME for now we only support childs with simpleType (but it can be a complexType)
							taskInfo.addOutputData(parameter.getName(), childEl.getAttributeValue("name"), dataModelURI, dataModelID, childTypeElString, null, value);
						}
					}	
				}
			}
		}
	}
	
	protected Element getParameterTypeWithName(Element schema, String name) {
		Element paramType = null;
		for (Object child : schema.getChildren()) {
			Element childEl = (Element) child;
			if(childEl.getAttributeValue("name").equals(name)) {
				paramType = childEl;
				break;
			}
		}
		
		return paramType;
	}
	
	protected TaskInfo buildPreActivityTaskInfo(WorkItemRecord wir, ArrayList<DataInfo> skippedData) {
		TaskInfo taskInfo = new TaskInfo(TaskInfo.TASK_TYPE_ACTIVITY, wir.getIDForDisplay());
		taskInfo.setTaskIDForDisplay("[PRE]" + wir.getIDForDisplay());
		
		taskInfo.isPreActivity(true);
		
		for (DataInfo dataInfo : skippedData) {
			if(dataInfo.getDataNameURI().isDataAttribute()) {
			String entityName = dataInfo.getDataNameURI().getEntityName();
			String attributeName = dataInfo.getDataNameURI().getAttributeName();
				// add output data to task info
			taskInfo.addOutputData(entityName, attributeName, dataInfo.getDataModelURI(), dataInfo.getInstanceID(), dataInfo.getDataType(), dataInfo.getRestrictions(), dataInfo.getValue());
			} else {
				//it is an entity
				// get the key attributes of the entity
				ArrayList<DataInfo> vars = ConditionsInterpreter.get().getDataInfo(dataInfo.getDataNameURI(), dataInfo.getDataModelURI(), dataInfo.getInstanceID());
				ArrayList<DataInfo> keyVars = new ArrayList<DataInfo>();
				for (DataInfo var : vars) {
					if(var.isKey() && !var.isDefined()) {
						keyVars.add(var);
					}
				}
				//insert all the attributes
				for (DataInfo keyVar : keyVars) {
					taskInfo.addOutputData(keyVar.getDataNameURI().getEntityName(), 
							keyVar.getDataNameURI().getAttributeName(),
							keyVar.getDataModelURI(), keyVar.getInstanceID(), 
							keyVar.getDataType(), keyVar.getRestrictions(), keyVar.getValue());
				}
				
			}
			
		}
		return taskInfo;
	}
}
