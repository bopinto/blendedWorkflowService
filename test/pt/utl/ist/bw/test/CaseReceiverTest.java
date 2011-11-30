package pt.utl.ist.bw.test;

import java.io.File;

import org.junit.Test;
import static org.mockito.Mockito.*;

import pt.utl.ist.bw.ControlPanel;
import pt.utl.ist.bw.utils.CaseReceiver;
import pt.utl.ist.bw.utils.StringUtils;

public class CaseReceiverTest {

	@Test
	public void testUploadSpec() {
		ControlPanel mockedControlPanel = mock(ControlPanel.class);
		CaseReceiver caseReceiver = new CaseReceiver(mockedControlPanel);
		
		File f = new File("/tmp/MedicalEpisode.xml");
		
		String specInString = StringUtils.fileToString(f);
		when(caseReceiver.getActivitySpecReceiver().getSpecInString()).thenReturn(specInString);
		
		File gf = new File("/tmp/BWMedicalEpisode.xml");
		String goalSpecInString = StringUtils.fileToString(gf);
		when(caseReceiver.getBWSpecReceiver().getSpecInString()).thenReturn(goalSpecInString);
		
		
	}
}
