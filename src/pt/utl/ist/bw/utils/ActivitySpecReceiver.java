package pt.utl.ist.bw.utils;

import pt.utl.ist.bw.ControlPanel;

import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class ActivitySpecReceiver extends SpecReceiver {

	private String specInString = null;
	private String specName = null;
	
	public ActivitySpecReceiver(ControlPanel ctrPanel) {
		super(ctrPanel);
	}
	
	public String getSpecInString() {
		return this.specInString;
	}
	public String getSpecName() {
		return this.specName;
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		this.specInString = StringUtils.bufferToString(super.buffer);
		
		if(specInString == null) {
			this.ctrlPanel.getWindow().showNotification("Submition of " + event.getFilename() + " failed", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		this.specName = event.getFilename();
	}
}
