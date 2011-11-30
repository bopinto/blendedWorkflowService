package pt.utl.ist.bw.conditionsinterpreter.conditions;


public abstract class AbstractCondition extends Condition {
	
	public TripleStateBool evaluate(DataObservable otherData) {
		return TripleStateBool.FALSE;
	}
	
	public TripleStateBool evaluate() {
		return TripleStateBool.FALSE;
	}
	
	public Condition and(Condition otherCondition) {
		return new AndCondition(this, otherCondition);
	}
	
	public Condition or(Condition otherCondition) {
		return new OrCondition(this, otherCondition);
	}
	
	public Condition not() {
		return new NotCondition(this);
	}

}
