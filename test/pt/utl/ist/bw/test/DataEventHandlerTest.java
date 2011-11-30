package pt.utl.ist.bw.test;

import static org.junit.Assert.*;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.bw.conditionsinterpreter.DataEventHandlerClient;

public class DataEventHandlerTest {
	
	private DataEventHandlerClient dataEventHandlerClient = null;
	
	@Before
	public void setUp() {
		this.dataEventHandlerClient = new DataEventHandlerClient("http://localhost:8080/DataRepository/bw");
	}
	
	@Test
	public void testCreateNewModelInstance() {
//		String result = null;
//		try {
//			result = this.dataEventHandlerClient.createNewModelInstance("0", "0");
//		} catch (IOException e) {
//			fail("IOException");
//		}
//		
//		Assert.assertNotNull(result);
//		Assert.assertTrue(!result.contains("<failure>"));
	}

}
