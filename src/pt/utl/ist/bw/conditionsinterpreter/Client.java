package pt.utl.ist.bw.conditionsinterpreter;

public class Client {

	private String username;
	private String password;
	
	public Client(String user, String pass) {
		this.username = user;
		this.password = pass;
	}
	
	/////// GETTERS ///////
	public String getUsername() { return this.username; }
	public String getPassword() { return this.password; }
	///////////////////////
	
}
