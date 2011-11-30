package pt.utl.ist.bw.elements;

public class CaseIDForDisplay {
	
	private CaseInstanceID instanceID;
	private CaseURI uri;
	
	public CaseIDForDisplay(CaseInstanceID id, CaseURI uri) {
		this.instanceID = id;
		this.uri = uri;
	}
	
	public CaseInstanceID getCaseInstanceID() { return this.instanceID; }
	public CaseURI getCaseURI() { return this.uri; }
	
	@Override
	public String toString() {
		return instanceID.toString() + ":" + uri.toString();
	}
	
	@Override
	public boolean equals(Object anObject) {
		if(!(anObject instanceof CaseIDForDisplay)) {
			return false;
		}
		return ((CaseIDForDisplay)anObject).getCaseInstanceID().toString().equals(this.instanceID.toString()) &&
		((CaseIDForDisplay)anObject).getCaseURI().toString().equals(this.uri.toString()) ?
				true : false;
	}
	
	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + instanceID.hashCode();
		hash = hash * 31 + uri.hashCode();
		return hash;
	}

}
