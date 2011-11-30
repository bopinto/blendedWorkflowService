package pt.utl.ist.bw.conditionsinterpreter.conditions;

import java.util.ArrayList;

import pt.utl.ist.bw.messages.DataInfo;

public class NotCondition extends AbstractCondition {

	private Condition condition;
	
	public NotCondition(Condition cond) {
		this.condition = cond;	
	}
	
	@Override
	public ArrayList<DataInfo> getData() {
		return condition.getData();
	}
	
	@Override
	public TripleStateBool evaluate() {
		return this.condition.evaluate().NOT();
	}
	
	@Override
	public ArrayList<DataInfo> getKeyData() {
		return condition.getKeyData();
	}

}
