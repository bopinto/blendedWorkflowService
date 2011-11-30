package pt.utl.ist.bw.messages;

import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;

public class EntityInfo {
	
	private DataModelInstanceID instanceID;
	private DataModelURI dataModelURI;

	private DataNameURI dataURI;
	
	private boolean isSkipped;
	private boolean isDefined;
	
	public EntityInfo(){}
	
	public EntityInfo(DataModelURI dataModelURI, DataNameURI dataName, String dataType, String restrictions) {
		this.dataModelURI = dataModelURI;
		this.dataURI = dataName;
	}
	
	public EntityInfo(DataModelURI dataModelURI, DataNameURI dataName, String dataType, String restrictions, String startValue) {
		this.dataModelURI = dataModelURI;
		this.dataURI = dataName;
	}
	
	//****** GETTERS *******//
	public DataModelURI getDataModelURI() { return this.dataModelURI; }
	public DataModelInstanceID getInstanceID() { return this.instanceID; }
	public DataNameURI getDataNameURI() { return dataURI; }
	public String getDataName() { return this.dataURI.getAttributeName(); }
	public boolean isSkipped() { return this.isSkipped; }
	public boolean isDefined() { return this.isDefined; }
	/////////////////////////
	
	//******** SETTERS *******//
	public void setDataModelURI(DataModelURI uri) { this.dataModelURI = uri; }
	public void setInstanceID(DataModelInstanceID id) { this.instanceID = id; }
	public void setDataNameURI(DataNameURI dataName) { this.dataURI = dataName; }
	public void isSkipped(boolean skipped) { this.isSkipped = skipped; }
	public void isDefined(boolean defined) { this.isDefined = defined; }
	////////////////////////////
}
