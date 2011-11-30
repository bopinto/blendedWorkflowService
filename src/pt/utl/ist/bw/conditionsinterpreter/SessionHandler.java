package pt.utl.ist.bw.conditionsinterpreter;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.yawlfoundation.yawl.util.PasswordEncryptor;

@SuppressWarnings("serial")
public class SessionHandler extends ConcurrentHashMap<String, Session>{

	private HashMap<String, Client> validUsers = new HashMap<String, Client>();
	
	private static SessionHandler instance;
	
	private SessionHandler() {
		String encriptedPass = PasswordEncryptor.encrypt("datarepositorypass", null);
		Client dataRep = new Client("datarepository", encriptedPass);
		this.validUsers.put("datarepository", dataRep);
	}
	
	public static SessionHandler get() {
		if(instance == null) {
			instance = new SessionHandler();
		}
		return instance;
	}
	
	public String createNewSession(String username, String pass, long timeout) {
		Client user= this.validUsers.get(username);
		if(user == null) {
			return null;
		}
		if(!user.getPassword().equals(pass)) {
			return null;
		}
		
		Session dataSession = new Session(user, timeout);
		String handle = dataSession.getHandle();
		this.put(handle, dataSession);
		return handle;
	}
	
	public boolean checkConnection(String handle) {
		boolean result = false;
		if(handle != null) {
			Session dataSession = this.get(handle);
			if(dataSession != null) {
				dataSession.resetTimer();
				result = true;
			}
		}
		return result;
	}
}
