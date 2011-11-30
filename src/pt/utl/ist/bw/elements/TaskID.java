package pt.utl.ist.bw.elements;

public class TaskID {
	
	private CaseURI uri;
	private String taskName;
	
	public TaskID(CaseURI uri, String taskName) {
		this.uri = uri;
		this.taskName = taskName;
	}
	
	public String getTaskName() { return this.taskName; }
	
	@Override
	public String toString() {
		return uri.toString() + ":" + taskName;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if(!(anObject instanceof TaskID)) {
			return false;
		}
		return ((TaskID) anObject).toString().equals(this.toString());
	}

	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + uri.hashCode();
		hash = hash * 31 + taskName.hashCode();
		return hash;
	}
	
}
