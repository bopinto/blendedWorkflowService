package pt.utl.ist.bw.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import pt.utl.ist.bw.ControlPanel;

import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public abstract class SpecReceiver implements Receiver, FailedListener, SucceededListener{

	protected ControlPanel ctrlPanel = null;
	
	protected ByteArrayOutputStream buffer;
	
	protected Logger log;
	
	public SpecReceiver(ControlPanel ctrPanel) {
		this.ctrlPanel = ctrPanel;
		log = Logger.getLogger(SpecReceiver.class);
	}
	
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		this.buffer = new ByteArrayOutputStream();
		
		return buffer;
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		this.ctrlPanel.getWindow().showNotification("Specification " + event.getFilename() + " successfully uploaded");
	}

	@Override
	public void uploadFailed(FailedEvent event) {
		this.ctrlPanel.getWindow().showNotification("Upload of " + event.getFilename() + " failed", Notification.TYPE_ERROR_MESSAGE);
	}

}
