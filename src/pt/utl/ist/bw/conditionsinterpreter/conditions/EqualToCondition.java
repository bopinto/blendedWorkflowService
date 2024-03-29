package pt.utl.ist.bw.conditionsinterpreter.conditions;

import java.util.ArrayList;

import pt.utl.ist.bw.conditionsinterpreter.ConditionsInterpreter;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;
import pt.utl.ist.bw.messages.DataInfo;


public class EqualToCondition extends AbstractCondition {

	private DataModelURI dataModelURI;
	private DataModelInstanceID dataModelInstanceID;
	private DataNameURI data;
	private String value;

	public EqualToCondition(DataModelURI dataModelURI, DataNameURI data, DataModelInstanceID instanceID, String value) {
		this.dataModelURI = dataModelURI;
		this.data = data;
		this.value = value;
		this.dataModelInstanceID = instanceID;
	}
	
	@Override
	public TripleStateBool evaluate() {
		// get the current data info
		ArrayList<DataInfo> dataInfo = ConditionsInterpreter.get().getDataInfo(this.data, this.dataModelURI, this.dataModelInstanceID);
		
		// compare with the value
		for (DataInfo dataAtt : dataInfo) { //FIXME it is true if it finds a value that is equal. Same for skip. What if we want a specific instance?
			if(dataAtt.isSkipped()) {
				return TripleStateBool.SKIPPED;
			} else if(this.value.equals(dataAtt.getValue())) {
				return TripleStateBool.TRUE;
			}
		}
		return TripleStateBool.FALSE;
	}

	@Override
	public ArrayList<DataInfo> getData() {
		ArrayList<DataInfo> dataL = new ArrayList<DataInfo>();
		dataL.addAll(ConditionsInterpreter.get().getDataInfo(this.data, this.dataModelURI, this.dataModelInstanceID));
		return dataL;
	}
	
	@Override
	public ArrayList<DataInfo> getKeyData() {
		return this.getData();
	}
}
