package pt.utl.ist.bw.test;

import org.junit.Before;
import org.junit.Test;

import pt.utl.ist.bw.BlendedworkflowserviceApplication;
import pt.utl.ist.bw.messages.TaskInfo;
import static org.mockito.Mockito.*;

public class WorklistManagerTest {
	
	private BlendedworkflowserviceApplication app = null;
	
	@Before
	public void setUp() {
		
	}
	
	@Test
	public void testRegisterExecutedTaskGoal() {
		TaskInfo taskInfo = createTaskInfoMock();
		
		
		
	}
	
	protected TaskInfo createTaskInfoMock() {
		TaskInfo mockedTaskInfo = mock(TaskInfo.class);
		when(mockedTaskInfo.getTaskName()).thenReturn("0:Observe Patient");
		
		
		return mockedTaskInfo;
	}

}
