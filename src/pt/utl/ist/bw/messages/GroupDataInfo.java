package pt.utl.ist.bw.messages;

import java.util.ArrayList;

public class GroupDataInfo {

	private String groupName;
	private ArrayList<DataInfo> fields = new ArrayList<DataInfo>();

	public GroupDataInfo(String name) {
		this.groupName = name;
	}
	
	public String getGroupName() {
		return this.groupName;
	}
	
	public void addField(DataInfo field) {
		this.fields.add(field);
	}
	
	public ArrayList<DataInfo> getFields() {
		return this.fields;
	}
}
