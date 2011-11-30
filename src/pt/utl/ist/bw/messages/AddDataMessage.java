package pt.utl.ist.bw.messages;

public class AddDataMessage {

	private String dataName;
	private String dataType; // this only applies to attributes
	private boolean isKey;
	private String relatedEntity; // this only applies to entities
	
	public String getDataName() {
		return dataName;
	}
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public boolean isKey() {
		return isKey;
	}
	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}
	public String getRelatedEntity() {
		return relatedEntity;
	}
	public void setRelatedEntity(String relatedEntity) {
		this.relatedEntity = relatedEntity;
	}
}
