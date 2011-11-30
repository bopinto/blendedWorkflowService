package pt.utl.ist.bw.utils;

public class Var {
	
	private int index = -1;
	private String name = null;
	private String type = null;
	private String namespace = "http://www.w3.org/2001/XMLSchema";
	private boolean isInputVar = false;
	private boolean isOutputVar = false;
	
	public Var(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getNamespace() {
		return this.namespace;
	}
	public void setNamespace(String ns) {
		this.namespace = ns;
	}
	public int getIndex() {
		return this.index;
	}
	public void setIndex(int i) {
		this.index = i;
	}
	public boolean isInputVar() {
		return this.isInputVar;
	}
	public void isInputVar(boolean isInput) {
		this.isInputVar = isInput;
	}
	public boolean isOutputVar() {
		return this.isOutputVar;
	}
	public void isOutputVar(boolean isOutput) {
		this.isOutputVar = isOutput;
	}
	
	//******************************//
	public Var clone() {
		Var newVar = new Var(this.name, this.type);
		
		newVar.setIndex(this.index);
		newVar.setNamespace(this.namespace);
		newVar.isInputVar(this.isInputVar);
		newVar.isOutputVar(this.isOutputVar);
		
		return newVar;
	}
}
