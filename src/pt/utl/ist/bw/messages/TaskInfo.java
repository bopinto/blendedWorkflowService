package pt.utl.ist.bw.messages;

import java.util.ArrayList;

import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;

/**
 * Task info.
 * @author bernardoopinto
 *
 */
public class TaskInfo {
	
	public static String TASK_TYPE_GOAL = "Goal";
	public static String TASK_TYPE_ACTIVITY = "Activity";
	private String TASK_TYPE;
	
	private String taskName;
	private String taskIDForDisplay;
	
	private ArrayList<GroupDataInfo> inputData = new ArrayList<GroupDataInfo>();
	private ArrayList<GroupDataInfo> outputData = new ArrayList<GroupDataInfo>();
	
	private boolean isPreActivity = false;
	
	public TaskInfo() {}
	
	/**
	 * Constructor
	 * @param taskType must be TASK_TYPE_GOAL or TASK_TYPE_ACTIVITY
	 */
	public TaskInfo(String taskType, String taskName) {
		this.TASK_TYPE = taskType;
		this.taskName = taskName;
		this.taskIDForDisplay = taskName;
	}
	
	/**
	 * Adds an input field to the task
	 * @param dataGroupName the data group name (aka an entity in the data model)
	 * @param dataName the data name (aka an attribute in the data model)
	 * @param dataModelID the instance ID of the data model
	 * @param dataType the data type
	 * @param restrictions possible restrictions for the data (taken from the condition type)
	 * @param initialValue the initial value of the data field
	 */
	public void addInputData(String dataGroup, String dataField, DataModelURI dataModelURI, 
			DataModelInstanceID dataModelID, String dataType, 
			String restrictions, String initialValue) {
		
		String typeOfData = translateDataType(dataType);
		
		DataInfo newData = new DataInfo(dataModelURI, new DataNameURI(dataGroup, dataField), typeOfData, restrictions, initialValue);
		newData.setInstanceID(dataModelID);
		GroupDataInfo group = null;
		if(dataGroup == null) dataGroup = "";

		for (GroupDataInfo inputGroup : this.inputData) {
			if(inputGroup.getGroupName().equals(dataGroup)) {
				group = inputGroup;
				break;
			}
		}
		if(group == null) {
			group = new GroupDataInfo(dataGroup);
			this.inputData.add(group);
		}
		group.addField(newData);

	}
	
	/**
	 * Adds an output field to the task
	 * @param dataGroupName the data group name (aka an entity in the data model)
	 * @param dataName the data name (aka an attribute in the data model)
	 * @param dataModelID the instance ID of the data model
	 * @param dataType the data type
	 * @param restrictions possible restrictions for the data (taken from the condition type)
	 * @param initialValue the initial value of the data field
	 */
	public void addOutputData(String dataGroup, String dataField, 
			DataModelURI dataModelURI, DataModelInstanceID dataModelID, 
			String dataType, String restrictions, String initialValue) {
		String typeOfData = translateDataType(dataType);
		DataInfo newData = new DataInfo(dataModelURI, new DataNameURI(dataGroup, dataField), typeOfData, restrictions, initialValue);
		newData.setInstanceID(dataModelID);
		GroupDataInfo group = null;
		if(dataGroup == null) dataGroup = "";
		for (GroupDataInfo outputGroup : this.outputData) {
			if(outputGroup.getGroupName().equals(dataGroup)) {
				group = outputGroup;
				break;
			}
		}
		if(group == null) {
			group = new GroupDataInfo(dataGroup);
			this.outputData.add(group);
		}
		group.addField(newData);
	}
	
	public ArrayList<GroupDataInfo> getInputData() {
		return this.inputData;
	}
	
	public DataInfo getInputData(String groupName, String dataName) {
		DataInfo data = null;
		for (GroupDataInfo group : this.inputData) {
			if(group.getGroupName().equals(groupName)) {
				for (DataInfo dataInfo : group.getFields()) {
					if(dataInfo.getDataName().equals(dataName)) {
						data = dataInfo;
						return data;
					}
				}
			}
		}
		return data;
	}
	
	public ArrayList<GroupDataInfo> getOutputData() {
		return this.outputData;
	}
	
	public DataInfo getOutputData(String groupName, String dataName) {
		DataInfo data = null;
		for (GroupDataInfo group : this.outputData) {
			if(group.getGroupName().equals(groupName)) {
				for (DataInfo dataInfo : group.getFields()) {
					if(dataInfo.getDataName().equals(dataName)) {
						data = dataInfo;
						return data;
					}
				}
			}
		}
		return data;
	}
	
	public String getTaskType() {
		return this.TASK_TYPE;
	}
	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
		this.taskIDForDisplay = taskName;
	}
	
	public String getTaskName() {
		return this.taskName;
	}
	
	public void setTaskIDForDisplay(String taskIDForDisplay) {
		this.taskIDForDisplay = taskIDForDisplay;
	}
	public String getTaskIDForDisplay() {
		return this.taskIDForDisplay;
	}
	
	public void isPreActivity(boolean isPreActivity) {
		this.isPreActivity = isPreActivity;
	}
	public boolean isPreActivity() { return this.isPreActivity; }
	
	protected String translateDataType(String dataType) {
		if(dataType.equalsIgnoreCase("boolean") || dataType.equalsIgnoreCase("xs:boolean")) {
			return "BOOLEAN";
		} else {
			return "STRING";
		}
	}
	
}
