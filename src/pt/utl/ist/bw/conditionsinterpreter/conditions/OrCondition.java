package pt.utl.ist.bw.conditionsinterpreter.conditions;

import java.util.ArrayList;

import pt.utl.ist.bw.messages.DataInfo;

public class OrCondition extends AbstractCondition {

	private Condition one;
	private Condition other;
	
	public OrCondition(Condition one, Condition other) {
		this.one = one;
		this.other = other;
	}
	
	@Override
	public ArrayList<DataInfo> getData() {
		ArrayList<DataInfo> dataL = new ArrayList<DataInfo>(one.getData());
		dataL.addAll(other.getData());
		return dataL;
	}

	@Override
	public TripleStateBool evaluate() {
		return this.one.evaluate().OR(this.other.evaluate());
	}
	
	@Override
	public ArrayList<DataInfo> getKeyData() {
		ArrayList<DataInfo> dataL = new ArrayList<DataInfo>(one.getKeyData());
		dataL.addAll(other.getKeyData());
		return dataL;
	}
}
