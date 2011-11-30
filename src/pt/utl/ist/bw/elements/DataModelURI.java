package pt.utl.ist.bw.elements;

public class DataModelURI {
	
	private String uri;
	
	public DataModelURI(String uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return this.uri;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if(!(anObject instanceof DataModelURI)) {
			return false;
		}
		
		return ((DataModelURI)anObject).toString().equals(this.uri);
	}
	
	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + uri.hashCode();
		hash = hash * 31;
		return hash;
	}
}
