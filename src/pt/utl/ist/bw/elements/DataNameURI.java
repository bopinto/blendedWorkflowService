package pt.utl.ist.bw.elements;

public class DataNameURI {
	
	private String entityName;
	private String attributeName;
	
	public DataNameURI(String entityName, String attributeName) {
		this.entityName = entityName;
		this.attributeName = attributeName;
	}
	
	public DataNameURI(String elementURI) {
		String[] elementArr = elementURI.split("\\.");
		this.entityName = elementArr[0];
		if(elementArr.length > 1) {
			this.attributeName = elementArr[1];
		}
	}
	
	public String getEntityName() { 
		return (this.entityName == null) ? "" : this.entityName;
	}
	public String getAttributeName() { 
		return (this.attributeName == null) ? "" : this.attributeName; 
	}
	
	public boolean isDataEntity() {
		if(attributeName == null) {
			return true;
		}
		return false;
	}
	
	public boolean isDataAttribute() {
		if(attributeName == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		if(attributeName == null) {
			return entityName;
		}
		
		return entityName + "." + attributeName;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if(!(anObject instanceof DataNameURI)) {
			return false;
		}
		
		return ((DataNameURI) anObject).toString().equals(this.toString());
	}

	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + entityName == null ? 0 : entityName.hashCode();
		hash = hash * 31 + attributeName == null ? 0 : attributeName.hashCode();
		return hash;
	}
	
}
