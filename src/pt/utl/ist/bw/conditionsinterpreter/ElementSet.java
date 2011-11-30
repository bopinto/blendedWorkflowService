package pt.utl.ist.bw.conditionsinterpreter;

import java.util.ArrayList;

import pt.utl.ist.bw.elements.TaskURI;

public class ElementSet {

	public ArrayList<TaskURI> goals = new ArrayList<TaskURI>();
	public ArrayList<TaskURI> preconditions = new ArrayList<TaskURI>();
	public ArrayList<TaskURI> postconditions = new ArrayList<TaskURI>();
}
