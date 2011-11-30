package pt.utl.ist.bw;

import org.junit.Before;
import org.junit.Test;


public class ControlPanelTest {
	
	private ControlPanel controlPanel = null;
	
	@Before
	public void setUp() {
		controlPanel = new ControlPanel();
	}
	
	@Test
	public void testAddUploads() {
		controlPanel.setProcName("Medical Episode");
		controlPanel.addUploads();
		
	}

}
