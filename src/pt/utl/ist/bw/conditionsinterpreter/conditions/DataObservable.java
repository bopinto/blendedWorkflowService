package pt.utl.ist.bw.conditionsinterpreter.conditions;

public abstract class DataObservable {
	
	public abstract void register(DataObserver obs);
	public abstract void broadcastChange();
	public abstract String getName();

}
