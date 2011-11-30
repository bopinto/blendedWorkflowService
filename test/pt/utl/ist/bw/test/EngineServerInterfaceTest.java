package pt.utl.ist.bw.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.bw.utils.StringUtils;
import pt.utl.ist.goalengine.EngineServerInterface;
import pt.utl.ist.goalengine.GoalEngine;

public class EngineServerInterfaceTest {
	
	private String spec = null;
	
	@Before
	public void setUp() throws Exception {
		File file = new File("/tmp/BWMedicalEpisode.xml");
		this.spec = StringUtils.fileToString(file);
	}

	@Test
	public void testLoadSpecification() {
		boolean result = EngineServerInterface.get().loadSpecification(spec);
		Assert.assertTrue(result);
	}
}
