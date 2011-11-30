package pt.utl.ist.bw.conditionsinterpreter.conditions;

import java.util.ArrayList;

import pt.utl.ist.bw.messages.DataInfo;

public abstract class Condition extends DataObserver {
	
	public abstract TripleStateBool evaluate();
	
	public abstract ArrayList<DataInfo> getKeyData();
	public abstract ArrayList<DataInfo> getData();
	
	public abstract Condition and(Condition otherCondition);
	public abstract Condition or(Condition otherCondition);
	public abstract Condition not();
}
