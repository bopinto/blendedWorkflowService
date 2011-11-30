package pt.utl.ist.bw.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.bw.conditionsinterpreter.ConditionEvaluator;
import pt.utl.ist.bw.conditionsinterpreter.DataEventHandlerClient;
import pt.utl.ist.bw.conditionsinterpreter.conditioncompiler.ConditionFactory;
import pt.utl.ist.bw.conditionsinterpreter.conditions.Condition;
import pt.utl.ist.bw.conditionsinterpreter.conditions.TripleStateBool;
import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;
import pt.utl.ist.bw.exceptions.InvalidConditionException;
import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.EntityInfo;

// because we're having a little bit of trouble putting this to work
public class CheckInPatientPostCondTest {
	
	private DataModelInstanceID instanceID;
	private DataEventHandlerClient eventHandler = new DataEventHandlerClient("http://localhost:8080/dataRepository/bw"); 
	private DataModelURI dataModelURI = new DataModelURI("MedicalEpisodeDataModel");
	private CaseURI yawlSpecURI = new CaseURI("MedicalEpisode");
	private CaseInstanceID yawlInstanceID = new CaseInstanceID("50");
	private CaseURI goalSpecURI = new CaseURI("Medical_Episode_GSpec_0");
	private CaseInstanceID goalInstanceID = new CaseInstanceID("0");
	
	@Before
	public void setUp() {
		try {
			this.instanceID = eventHandler.createNewModelInstance(this.dataModelURI, 
					yawlSpecURI, yawlInstanceID, goalSpecURI, goalInstanceID);
		
			DataInfo dataInfo = new DataInfo();
			dataInfo.setDataModelURI(dataModelURI);
			dataInfo.setInstanceID(instanceID);
			dataInfo.setDataNameURI(new DataNameURI("Patient.Name"));
			dataInfo.setDataType("STRING");
			dataInfo.setValue("John");
			dataInfo.setRestrictions(null);
			dataInfo.isSkipped(false);
			eventHandler.submitData(dataInfo);
		} catch(IOException ioe) {}
	}
	
	@Test
	public void testPatientAndEpisodeDefined() {
		try {
			EntityInfo patient = eventHandler.getEntityInfo(dataModelURI, new DataNameURI("Patient"), instanceID);
			
			assertTrue(patient.isDefined());
			
			EntityInfo episode = eventHandler.getEntityInfo(dataModelURI, new DataNameURI("Episode"), instanceID);
			
			assertTrue(episode.isDefined());
		} catch (IOException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void testCheckInPostCondition() {
		String condInString = "exists(Patient) and exists(Episode)";
		
		try {
			Condition cond = ConditionFactory.createCondition(dataModelURI, condInString, instanceID);
		
			assertEquals(TripleStateBool.TRUE, cond.evaluate());
			
		} catch (InvalidConditionException e) {
			fail(e.toString());
		}
		
	}

}
