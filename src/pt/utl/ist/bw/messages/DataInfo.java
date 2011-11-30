package pt.utl.ist.bw.messages;

import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;

/**
 * The info about data (that is part of a form)
 * @author bernardoopinto
 *
 */
public class DataInfo extends EntityInfo {
	
	private String dataType;
	private String value = null;
	private String restrictions;
	
	private boolean isKey = false;
	
	public DataInfo(){}
	
	public DataInfo(DataModelURI dataModelURI, DataNameURI dataName, String dataType, String restrictions) {
		super.setDataModelURI(dataModelURI);
		super.setDataNameURI(dataName);
		this.dataType = dataType;
		this.restrictions = restrictions;
		this.value = null;
	}
	
	public DataInfo(DataModelURI dataModelURI, DataNameURI dataName, String dataType, String restrictions, String startValue) {
		super.setDataModelURI(dataModelURI);
		super.setDataNameURI(dataName);
		this.dataType = dataType;
		this.restrictions = restrictions;
		this.value = startValue;
	}
	
	//****** GETTERS *******//
	public DataModelURI getDataModelURI() { return super.getDataModelURI(); }
	public DataModelInstanceID getInstanceID() { return super.getInstanceID(); }
	public DataNameURI getDataNameURI() { return super.getDataNameURI(); }
	public String getDataName() { return super.getDataNameURI().getAttributeName(); }
	public String getDataType() { return dataType; }
	public String getRestrictions() { return restrictions; }
	public String getValue() { 
		if(this.value == null && this.dataType.equalsIgnoreCase("BOOLEAN")) {
			return Boolean.toString(false);
		}
		return this.value; 
	}
	public boolean isSkipped() { return super.isSkipped(); }
	public boolean isDefined() { return super.isDefined(); }
	public boolean isKey() { return this.isKey; }
	/////////////////////////
	
	//******** SETTERS *******//
	public void setDataModelURI(DataModelURI uri) { super.setDataModelURI(uri); }
	public void setInstanceID(DataModelInstanceID id) { super.setInstanceID(id); }
	public void setDataNameURI(DataNameURI dataName) { super.setDataNameURI(dataName); }
	public void setDataType(String dataType) { this.dataType = dataType; }
	public void setRestrictions(String restrictions) { this.restrictions = restrictions; }
	public void setValue(String value) { this.value = value; }
	public void isSkipped(boolean skipped) { super.isSkipped(skipped); }
	public void isDefined(boolean defined) { super.isDefined(defined); }
	public void isKey(boolean isKey) { this.isKey = isKey; }
	////////////////////////////
	
}
