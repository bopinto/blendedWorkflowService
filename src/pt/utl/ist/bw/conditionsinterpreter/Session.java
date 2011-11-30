package pt.utl.ist.bw.conditionsinterpreter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Session {
	
	private String handle;
	private long interval;
	
	private Timer activityTime;
	
	private Client client;
	
	public Session(Client cli, long timeout) {
		this.handle = UUID.randomUUID().toString();
		this.interval = getIntervalInMilis(timeout);
	}

	public String getHandle() { return this.handle; }
	public Client getClient() { return this.client; }
	
	public void resetTimer() {
		// cancel
		if(this.activityTime != null) {
			this.activityTime.cancel();
		}
		
		// start
		startActivityTime();
	}
	
	protected long getIntervalInMilis(long seconds) {
		if(seconds == 0) {
			return 3600000;
		}
		return seconds * 1000;
	}
	
	protected void startActivityTime() {
		if(this.interval > 0) {
			this.activityTime = new Timer();
			TimerTask tTask = new TimeOut();
			this.activityTime.schedule(tTask, this.interval);
		}
	}
	
	private class TimeOut extends TimerTask {
		public void run() {
			SessionHandler.get().remove(handle);
		}
	}

}
