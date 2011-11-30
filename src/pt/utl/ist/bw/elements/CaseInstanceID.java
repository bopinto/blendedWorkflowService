package pt.utl.ist.bw.elements;

public class CaseInstanceID {
	
	private String instanceID;
	
	public CaseInstanceID(String id) {
		if(id == null) this.instanceID = "";
		if(id.contains(".")) {
			this.instanceID = id.split("\\.")[0];
		} else {
			this.instanceID = id;
		}
	}
	
	@Override
	public String toString() {
		return this.instanceID;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if(!(anObject instanceof CaseInstanceID)) {
			return false;
		}
		return ((CaseInstanceID) anObject).toString().equals(this.toString());
	}
	
	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + instanceID.hashCode();
		hash = hash * 31;
		return hash;
	}
}
