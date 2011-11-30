package pt.utl.ist.bw.conditionsinterpreter.conditions;

import java.util.ArrayList;

import pt.utl.ist.bw.messages.DataInfo;

public class TrueCondition extends AbstractCondition {

	public TrueCondition() {
	}
	
	@Override
	public TripleStateBool evaluate() {
		return TripleStateBool.TRUE;
	}
	
	@Override
	public ArrayList<DataInfo> getData() {
		ArrayList<DataInfo> dataL = new ArrayList<DataInfo>();
		return dataL;
	}
	
	@Override
	public ArrayList<DataInfo> getKeyData() {
		return this.getData();
	}
}
