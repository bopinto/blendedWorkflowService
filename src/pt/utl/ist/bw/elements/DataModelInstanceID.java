package pt.utl.ist.bw.elements;

public class DataModelInstanceID {

	private String instanceID;
	
	public DataModelInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}
	
	@Override
	public String toString() {
		return this.instanceID;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if(!(anObject instanceof DataModelInstanceID)) {
			return false;
		}
		return ((DataModelInstanceID)anObject).toString().equals(this.instanceID);
	}
	
	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + instanceID.hashCode();
		hash = hash * 31;
		return hash;
	}
}
