package pt.utl.ist.bw.elements;

public class TaskIDForDisplay {
	
	private CaseInstanceID instanceID;
	private String taskName;
	
	public TaskIDForDisplay(CaseInstanceID id, String taskName) {
		this.instanceID = id;
		this.taskName = taskName;
	}
	
	@Override
	public String toString() {
		return instanceID.toString() + ":" + taskName;
	}
	
	@Override
	public boolean equals(Object anObject) {
		return true;
	}

	@Override
	public int hashCode() { 
		int hash = 1;
		hash = hash * 31 + instanceID.hashCode();
		hash = hash * 31 + taskName.hashCode();
		return hash;
	}
}
