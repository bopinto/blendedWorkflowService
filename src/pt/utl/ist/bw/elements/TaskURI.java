package pt.utl.ist.bw.elements;

public class TaskURI {
	
	private CaseInstanceID instanceID;
	private CaseURI uri;
	private String taskName;
	
	public TaskURI(CaseInstanceID instanceID, CaseURI uri, String taskName) {
		this.instanceID = instanceID;
		this.uri = uri;
		this.taskName = taskName;
	}
	
	public String getTaskIDForDisplay() {
		return instanceID.toString() + ":" + taskName;
	}
	
	@Override
	public String toString() {
		return this.instanceID.toString() + ":" + uri.toString() + ":" + taskName;
	}
	
	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + instanceID.hashCode();
		hash = hash * 31 + uri.hashCode();
		hash = hash * 31 + taskName.hashCode();
		return hash;
	}

}
