package pt.utl.ist.bw.elements;

public class CaseURI {
	
	private String uri;
	
	public CaseURI(String uri) {
		this.uri = uri;
	}
	
	@Override
	public String toString() {
		return this.uri;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if(!(anObject instanceof CaseURI)) {
			return false;
		}
		return ((CaseURI) anObject).toString().equals(this.uri);
	}
	
	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + uri.hashCode();
		hash = hash * 31;
		return hash;
	}

}
