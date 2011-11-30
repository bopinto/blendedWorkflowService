package pt.utl.ist.bw.bwactivitymanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.util.JDOMUtil;

import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.GroupDataInfo;
import pt.utl.ist.bw.messages.TaskInfo;
import pt.utl.ist.bw.utils.SpecUtils;
import pt.utl.ist.bw.utils.StringUtils;

/**
 * Routes the events from and to the Managers (coming from YAWL).
 * Also, keeps a register of cases and tasks (to know to who redirect the requests). 
 * @author bernardoopinto
 *
 */
public class ActivityEventRouter extends InterfaceBWebsideController {

	// db of specs
	private ArrayList<YSpecificationID> loadedActivitySpecs = new ArrayList<YSpecificationID>(); // spec UUID
	private ArrayList<YSpecificationID> loadedGoalSpecs = new ArrayList<YSpecificationID>();
	
	//db of active work items
	private HashMap<WorkItemRecord, WorkItemRecord> activeWorkItems = new HashMap<WorkItemRecord, WorkItemRecord>(); //key = old wir (before checkout), value = new wir (after checkout)
	private ArrayList<WorkItemRecord> enabledWorkItems = new ArrayList<WorkItemRecord>();
	
    // required data for interfacing with the engine
    protected String engineUser = "blendedWorkflowService";
    protected String enginePassword = "yBW";
    protected String sessionHandle = null;
    protected String engineURI = "http://localhost:8080/yawl/ib";
    protected String bwURI = "http://localhost:8080/blendedWorkflowService/ib";
    private InterfaceB_EnvironmentBasedClient interfaceBClient; //we need this to do some extra work
    private InterfaceA_EnvironmentBasedClient interfaceAClient;
    
    private static Logger log ;                        // debug log4j file
    private static ActivityEventRouter instance;
    
    public ActivityEventRouter() {
    	super();
    	log = Logger.getLogger(ActivityEventRouter.class);
    	
    	this.interfaceBClient = new InterfaceB_EnvironmentBasedClient(this.engineURI);
    	this.interfaceAClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
    	super.setUpInterfaceBClient(this.engineURI);
    }
    
    public static ActivityEventRouter getInstance() {
    	if(instance == null) {
    		instance = new ActivityEventRouter();
    	}
    	return instance;
    }
    
    protected void setbwURI() {
        if (connected()) {
            Set<YAWLServiceReference> services =
                    interfaceAClient.getRegisteredYAWLServices(sessionHandle) ;
            if (services != null) {
                for (YAWLServiceReference service : services) {
                    if (service.getURI().contains("blendedWorkflowService")) {
                        this.bwURI = service.getURI();
                    }
                }
            }
        }
    }
        
    /*************************************************************/
    
    /******************* TASK HANDLE (FROM YAWL) *****************/
    
    /**
     * Loads a specification into the YAWL Engine.
     * @param specID the specification UUID 
     * @param spec the specification (in a string)
     * @param yawlSpec is true if it is an activity specification or false if it is a goal specification. 
     * @return true if the specification is successfully loaded into the YAWL Engine or false otherwise.
     */
    public boolean loadSpecification(String specID, String spec, boolean yawlSpec) { //FIXME throw different exceptions instead of true/false
    	if(interfaceAClient == null) {
    		log.error("Cannot load the specification. Interface A is null");
    		return false;
    	}
    	if(specID == null) {
    		log.error("Cannot load the specification. The specification ID is null");
    		return false;
    	}
    	if(spec == null) {
    		log.error("Cannot load the specification. The specification is null");
    		return false;
    	}
    	
    	try{
    		List<SpecificationData> specList = getLoadedSpecs();
    		if(specList == null) {
    			log.error("Null specification list");
    			return false;
    		}
 
    		YSpecificationID yawlSpecId = SpecUtils.getYAWLSpecificationIDFromSpec(spec);
    		
    		for (SpecificationData specificationData : specList) {
				if(specificationData.getID().equals(yawlSpecId)) {
					log.info("Specification already loaded. Not loading again.");
					if(yawlSpec) {
    					this.loadedActivitySpecs.add(yawlSpecId);
    				} else {
    					this.loadedGoalSpecs.add(yawlSpecId);
    				}
					return true;
				}
			}
    		
    		if(connected()) {
    			String result = interfaceAClient.uploadSpecification(spec, this.sessionHandle);
    			if(successful(result)) {
    				log.info("Specification " + specID + " correctly uploaded to YAWL");
    				// add the spec to the loaded specs
    				if(yawlSpec) {
    					this.loadedActivitySpecs.add(yawlSpecId);
    				} else {
    					this.loadedGoalSpecs.add(yawlSpecId);
    				}
    				return true;
    			}
    			else {
    				log.error("Specification " + specID + " was not correctly uploaded.");
    				return false;
    			}
    		} else {
    			log.error("Could not connect to server");
    			return false;
    		}
    	} catch(IOException ioe) {
    		log.error("IOException: Specification " + specID + " was not correctly uploaded");
    		return false;
    	}
    }
    
    /**
     * Unloads a specification from the Engine
     * @param specID the specification UUID
     * @return true if the specification is successfully unloaded
     */
    public boolean unloadSpecification(String specID, boolean isActivitySpecification) {
    	if(specID == null) {
    		log.error("Cannot unload the specification. The specification is null");
    		return false;
    	}
    	
    	try {
    		// get the specification ID
    		YSpecificationID ySpecID = null;
    		boolean found = false; 
    		
    		if(isActivitySpecification) {
    			for (YSpecificationID yspecidit : this.loadedActivitySpecs) {
    				if(yspecidit.getIdentifier().equals(specID)) {
    					ySpecID = yspecidit;
    					found = true;
    					break;
    				}
    			}
    		} else {
    			for (YSpecificationID yspecidit : this.loadedGoalSpecs) {
    				if(yspecidit.getIdentifier().equals(specID)) {
    					ySpecID = yspecidit;
    					found = true;
    					break;
    				}
    			}	
    		}
    		
    		if(!found) {
    			log.error("Could not find the given spec ID.");
    			return false;
    		}
    		
    		if(connected()) {
    			String result = this.interfaceAClient.unloadSpecification(ySpecID, this.sessionHandle);
    			if(successful(result)) {
    				if(isActivitySpecification) {
    					this.loadedActivitySpecs.remove(ySpecID);
    				} else {
    					this.loadedGoalSpecs.remove(ySpecID);
    				}
    				return true;
    			} else {
    				// if does not succeed, it may mean there are active cases
    				// get the case id from the work items
    				Collection<WorkItemRecord> workItemRecordCollection = this.activeWorkItems.values();
    				ArrayList<WorkItemRecord> workItemRecordList = new ArrayList<WorkItemRecord>(workItemRecordCollection);
    				String caseID = null;
    				for (WorkItemRecord workItemRecord : workItemRecordList) {
    					if(workItemRecord.getSpecIdentifier().equals(specID)) {
    						caseID = workItemRecord.getCaseID();
    						// cancel the case
    						this.interfaceBClient.cancelCase(caseID, this.sessionHandle); //FIXME brute force: I don't know if it worked.
    					}
    				}

    				// remove all the active work items from this case
    				workItemRecordCollection = this.activeWorkItems.keySet();
    				workItemRecordList = new ArrayList<WorkItemRecord>(workItemRecordCollection);

    				for (WorkItemRecord workItemRecord : workItemRecordList) {
    					if(workItemRecord.getSpecIdentifier().equals(specID)) {
    						this.activeWorkItems.remove(workItemRecord);
    					}
    				}
    				// unload the specification
    				this.interfaceAClient.unloadSpecification(ySpecID, this.sessionHandle); //FIXME brute force: I don't know if it worked.
    			}
    		} else {
    			log.error("Could not connect to YAWL engine");
    			return false;
    		}
		} catch (IOException e) {
			log.error("Could not reach the engine", e);
			return false;
		}
    	return true;
    }
    
    /**
     * Launches a case in the YAWL Engine
     * @param caseID the YAWL case UUID.
     * @param isActivityCase true is the case is an activity case.
     * @return the activity instance ID.
     */
    public String launchYAWLcase(String caseID, boolean isActivityCase) {
    	// get the loaded cases

    	List<SpecificationData> specList = getLoadedSpecs();
    	if(specList == null) {
    		log.error("Null specification list");
    		return null;
    	}

    	YSpecificationID yawlSpecId = null;
    	if(isActivityCase) {
    		for (YSpecificationID specId : this.loadedActivitySpecs) {
    			if(specId.getIdentifier().equals(caseID)) {
    				yawlSpecId = specId;
    				break;
    			}
    		}
    	} else {
    		for (YSpecificationID specId : this.loadedGoalSpecs) {
    			if(specId.getIdentifier().equals(caseID)) {
    				yawlSpecId = specId;
    				break;
    			}
    		}
    	}
    	if(yawlSpecId == null) {
    		log.error("The given caseId does not exist in the system.");
    		return null;
    	}

    	// get the loaded case with the given caseID (YSpecificationData)
    	SpecificationData specData = null;
    	for (SpecificationData specificationData : specList) {
    		if(specificationData.getID().equals(yawlSpecId)) {
    			specData = specificationData;
    			break;
    		}
    	}

    	if(specData == null) {
    		log.error("The given caseId is not loaded in the engine");
    		return null;
    	}
    	
    	try {
    		// get the case data
    		String caseData = null; //FIXME I have to get this but don't know from here

    		// build the case logger
    		YLogDataItem logData = new YLogDataItem("service", "name", "blendedWorkflowService", "string");
    		YLogDataItemList logDataList = new YLogDataItemList(logData);

    		if(connected()) {
    			// launch the case
    			String result = this.interfaceBClient.launchCase(yawlSpecId, caseData, logDataList, this.sessionHandle);
    			if(successful(result)) {
    				log.info("YAWL specification successfully launched");
    				return result;
    			} else {
    				log.error("Could not launch the YAWL specification");
    				return null;
    			}
    		} else {
    			log.error("could not contact the engine");
    			return null;
    		}
    	} catch (IOException e) {
    		log.error("could not contact the engine");
    		return null;
    	}
    }
    
    public void notifyActiveTasks(String caseInstanceID) {
    	List<WorkItemRecord> liveWorkItems = getLiveWorkItems(caseInstanceID);
    	
    	if(liveWorkItems != null) {
    		for (WorkItemRecord workItemRecord : liveWorkItems) {
    			if(!this.enabledWorkItems.contains(workItemRecord)) {
    				handleEnabledWorkItemEvent(workItemRecord);
    			}
    		}
    	}
    }
        
    protected List<WorkItemRecord> getLiveWorkItems(String caseInstanceID) {
    	try {
			return this.interfaceBClient.getWorkItemsForCase(caseInstanceID, this.sessionHandle);
		} catch (IOException e) {
			log.error("Could not get the live work items");
		}
		return null;
    }
    
    public Element getSpecDataSchema(YSpecificationID specID) {
    	try {
			String dataSchema = this.interfaceBClient.getSpecificationDataSchema(specID, this.sessionHandle);
			Document doc = StringUtils.stringToDoc(dataSchema);
			return doc.getRootElement();
		} catch (IOException e) {
			log.error("Could not retreive the data schema");
		}
		return null;
    }
    
	/**
	 * Handle tasks when they are enabled.
	 */
	@Override
	public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
		log.info("Handling enabled workitem event");
		
		// store the work item in the enabled workitems list
		this.enabledWorkItems.add(enabledWorkItem);
		
		// send the work item to the BW Activity manager
		BWActivityManager.get().registerEnabledTask(enabledWorkItem);
	}
	
	/**
     * Receives notification from the engine that an active case has been
     * completed.
     * @param caseID the id of the completed case.
     * @param casedata the set of net-level data for the case when it completes (in XML)
     */
	public void handleCompleteCaseEvent(String caseID, String casedata) {
		//TODO implement this?
	}

    /**
     *  Check the work item out of the engine
     *  @param wir - the work item to check out
     *  @return true if checkout was successful
     */
    public WorkItemRecord checkOutWorkItem(WorkItemRecord wir) {
    	if(!this.enabledWorkItems.contains(wir)) {
    		log.error("Unknown work item.");
    		return null;
    	}
    	
    	if(connected()) {
    		log.info("Connection to engine is active");
    		log.info("Checking out task " + wir.getTaskID());
    		
    		// do the checkout
    		WorkItemRecord checkedOutItem = null;
            try {
                 if ((checkedOutItem = checkOut(wir.getID(), this.sessionHandle)) != null) {
                      log.info("   checkout successful: " + wir.getID());
                      this.enabledWorkItems.remove(wir);
                      this.activeWorkItems.put(wir, checkedOutItem);
                      return checkedOutItem;
                 }
                 else {
                     log.info("   checkout unsuccessful: " + wir.getID());
                     return null;
                 }
             }
             catch (YAWLException ye) {
                 log.error("YAWL Exception with checkout: " + wir.getID(), ye);
                 return null;
             }
             catch (IOException ioe) {
                 log.error("IO Exception with checkout: " + wir.getID(), ioe);
                 return null;
             }
    	}
    	return null;
    }

    
    /**
     * Check-in a work item (in the engine)
	 * Stripped from DECLARE.
	 * @see http://www.win.tue.nl/declare/
     * @param wir the work item record
     */
    public boolean checkInWorkItem(WorkItemRecord wir, TaskInfo taskInfo) {
    	try {
    		if (connected()) {
    			TaskInformation taskInformation = getTaskInformation(wir);
    			Element task = this.prepareReplyRootElement(wir, taskInformation, sessionHandle);

    			Element outputData = this.outputData(taskInfo, task);
    			Element inputData = this.inputData(taskInfo, task);
    			
    			WorkItemRecord activatedWir = this.activeWorkItems.get(wir);
    			
    			if(activatedWir == null) {
    				Collection<WorkItemRecord> activeWirs = this.activeWorkItems.values();
    				ArrayList<WorkItemRecord> wirArr = new ArrayList<WorkItemRecord>(activeWirs);
    				for (WorkItemRecord workItemRecord : wirArr) {
						if(workItemRecord.getIDForDisplay().equals(wir.getIDForDisplay())) {
							activatedWir = workItemRecord;
							break;
						}
					}
    			}
    			
    			if(activatedWir == null) {
    				log.error("Could not find the given work item record");
    				return false;
    			}

    			String result = checkInWorkItem(activatedWir.getID(), inputData, outputData, null, sessionHandle); 
    			WorkItemRecord wirKey = null;
    			if (successful(result)){
    				log.info("Checked in " + wir.getIDForDisplay()); // log this event
    				
    				// if it is the value, get the key
    				
    				if(wir.equals(activatedWir)) {
    					for ( Map.Entry<WorkItemRecord, WorkItemRecord> entry : this.activeWorkItems.entrySet()) {
    						if(entry.getValue().getIDForDisplay().equals(activatedWir.getIDForDisplay())) {
    							wirKey = entry.getKey();
    							break;
    						}
    					}
    				}
    				if(wirKey == null) {
    					this.activeWorkItems.remove(activatedWir);
    				} else {
    					this.activeWorkItems.remove(wirKey);
    				}
    			} else {
    				log.error("Failed to check in " + wir.getIDForDisplay() + ": " + result); // log this event
    				return false;
    			}
    			if(wirKey == null) {
    				notifyActiveTasks(wir.getCaseID());
    			} else {
    				notifyActiveTasks(wirKey.getCaseID());
    			}
    			
    			return true;
    		}
    	}
    	catch (Exception e) {
    		log.error(e);
    	}
    	return false;
    }
    
	/**
	 * Handle tasks when they are canceled.
	 */
	@Override
	public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
		// TODO Auto-generated method stub
		
	}
	
	public void skipWorkItem(WorkItemRecord wir) {
		if(connected()) {
			try {
				String result = this.interfaceBClient.skipWorkItem(wir.getID(), this.sessionHandle);
				if(successful(result)) {
					log.info(result);
				} else {
					log.error(result);
				}
			} catch (IOException ioe) {
				log.error("Could not skip work item", ioe);
			}
		}
		
		notifyActiveTasks(wir.getCaseID());
	}
	
	public TaskInformation getTaskInformation(WorkItemRecord workItemRecord) {
		try {
			return super.getTaskInformation(new YSpecificationID(workItemRecord), workItemRecord.getTaskID(), this.sessionHandle);
		} catch(IOException e) {
			log.error("Could not get task information for task " + workItemRecord.getIDForDisplay(), e);
			return null;
		}
	}
	
	public void cancelCase() {
		//TODO stub method
	}
	
	/*************************************************************/

	/******************** INTERNAL METHODS ************************/
	
    /** Checks if there is a connection to the engine, and
     *  if there isn't, attempts to connect
     *  @return true if connected to the engine
     */
    protected boolean connected() {
        try {
            // if not connected
             if ((this.sessionHandle == null) || (!checkConnection(this.sessionHandle)))
                this.sessionHandle = connect(this.engineUser, this.enginePassword);
        }
        catch (IOException ioe) {
             log.error("Exception attempting to connect to engine", ioe);
        }
        if (!successful(this.sessionHandle)) {
            log.error(JDOMUtil.strip(this.sessionHandle));
        }
        return (successful(this.sessionHandle)) ;
    }
    
    protected List<SpecificationData> getLoadedSpecs() {
    	if(connected()) {
    		try {
    			return this.interfaceBClient.getSpecificationList(this.sessionHandle);
    		} catch(IOException ioe) {
    			log.error("IOException: Could not retreive specification list");
    			return null;
    		}
    	}
    	return null;
    }
	
    /**
     * Writes the input data in a XML Element
     * @param taskInfo the task info
     * @param root the root element to write the input data
     * @return the root element containing the input data
     */
    private Element inputData(TaskInfo taskInfo, Element root) {
    	Element input = new Element(root.getName());
        for (GroupDataInfo inputGroup : taskInfo.getInputData()) {
        	Element dataGroup = null;
        	if(inputGroup.getGroupName().equals("")) {
    			for (DataInfo data : inputGroup.getFields()) {
    				Element dataEl = new Element(data.getDataNameURI().getAttributeName());
    				dataEl.setText(data.getValue());
    				input.addContent(dataEl);
				}
    		} else {
    			dataGroup = new Element(inputGroup.getGroupName());
    			for (DataInfo data : inputGroup.getFields()) {
    				Element dataEl = new Element(data.getDataNameURI().getAttributeName());
    				dataEl.setText(data.getValue());
    				dataGroup.addContent(dataEl);
    			}
    			input.addContent(dataGroup);
    		}
		}
        return input;
    }


    /**
     * Stripped from DECLARE.
     * @see http://www.win.tue.nl/declare/
     *
     * @param external ExternalWorkItem
     * @param sessionHandle String
     * @return Element
     * @throws IOException
     */
    protected Element prepareReplyRootElement(WorkItemRecord wir, TaskInformation taskInfo, String sessionHandle) throws IOException {
    	Element replyToEngineRootDataElement;

    	//prepare reply root element.
    	SpecificationData sdata = getSpecificationData(new YSpecificationID(wir), sessionHandle);

    	String decompID = taskInfo.getDecompositionID();
    	if (sdata.usesSimpleRootData()) {
    		replyToEngineRootDataElement = new Element("data");
    	}
    	else {
    		replyToEngineRootDataElement = new Element(decompID);
    	}
    	return replyToEngineRootDataElement;
    }

    /**
     * Writes the output data in a XML Element
     * @param taskInfo the task info
     * @param root the root element to write the output data
     * @return the root element containing the output data
     */
    private Element outputData(TaskInfo taskInfo, Element root) {
    	Element output = new Element(root.getName());
    	for (GroupDataInfo outputGroup : taskInfo.getOutputData()) {
    		Element dataGroup = null;
    		if(outputGroup.getGroupName().equals("")) {
    			for (DataInfo data : outputGroup.getFields()) {
    				Element dataEl = new Element(data.getDataNameURI().getAttributeName());
    				dataEl.setText(data.getValue());
    				output.addContent(dataEl);
				}
    		} else {
    			dataGroup = new Element(outputGroup.getGroupName());
    			for (DataInfo data : outputGroup.getFields()) {
    				Element dataEl = new Element(data.getDataNameURI().getAttributeName());
    				dataEl.setText(data.getValue());
    				dataGroup.addContent(dataEl);
    			}
    			output.addContent(dataGroup);
    		}
    		
    	}
    	return output;
    }
    
}
