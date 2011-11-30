package org.yawlfoundation.yawl.elements.data.external;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

public class DataRepositoryGateway extends Interface_Client {
	private String backEndURI;

	private String engineUser = "yawl";
	private String enginePassword = "yawlpass";
	private String sessionHandle = null;

	private static Logger _log = Logger.getLogger(DataRepositoryGateway.class);

	public DataRepositoryGateway(String backEnd) {
		this.backEndURI = backEnd;
	}

	protected Element getElementInfo(String yawlCaseURI, String yawlCaseInstanceID, String elementName) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("getElementInfo", this.sessionHandle);
			params.put("yawlCaseURI", yawlCaseURI);
			params.put("instanceID", yawlCaseInstanceID);
			params.put("elemURI", elementName);

			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return JDOMUtil.stringToElement(result);
			} else {
				_log.error(result);
			}
		}

		return null;
	}

	protected boolean setElement(String paramName, Element element) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("setElement", this.sessionHandle);
			params.put("paramName", paramName);
			params.put("element", new XMLOutputter().outputString(element));
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return true;
			} else {
				_log.error(result);
			}
		}
		return false;
	}
	
	protected boolean registerYAWLActiveCase(String caseURI, String caseInstanceID, 
			String caseUUID, ArrayList<String> parameters) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("registerYAWLActiveCase", this.sessionHandle);
			params.put("caseURI", caseURI);
			params.put("caseInstanceID", caseInstanceID);
			params.put("caseUUID", caseUUID);
			params.put("varList", ArrayListToXML(parameters));
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return true;
			} else {
				_log.error(result);
			}
		}
		return false;
	}
	
	protected boolean unregisterYAWLActiveCase(String caseUUID, String caseInstanceID) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("unregisterYAWLActiveCase", this.sessionHandle);
			params.put("caseInstanceID", caseInstanceID);
			params.put("caseUUID", caseUUID);
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return true;
			} else {
				_log.error(result);
			}
		}
		return false;
	}

	////// INTERNAL METHODS ////////

	/** Checks if there is a connection to the engine, and
	 *  if there isn't, attempts to connect
	 *  @return true if connected to the engine
	 */
	protected boolean connected() {
		try {
			// if not connected
			if ((this.sessionHandle == null) || (!checkConnection()))
				this.sessionHandle = connect();
		}
		catch (IOException ioe) {
			_log.error("Exception attempting to connect to engine", ioe);
		}
		if (!successful(this.sessionHandle)) {
			_log.error(JDOMUtil.strip(this.sessionHandle));
		}
		return (successful(this.sessionHandle)) ;
	}

	protected boolean checkConnection() throws IOException {
		Map<String, String> params = prepareParamMap("checkConnection", this.sessionHandle);
		String msg = executePost(backEndURI, params);
		return successful(msg);
	}

	protected String connect() throws IOException {
		Map<String, String> params = prepareParamMap("connect", null);
		params.put("userID", this.engineUser);
		params.put("password", PasswordEncryptor.encrypt(this.enginePassword, null));
		return executePost(this.backEndURI, params);
	}
	
	private String ArrayListToXML(ArrayList<String> list) {
		Element array = new Element("ArrayList");
		for (String string : list) {
			array.addContent(new Element("Item").setText(string));
		}
		
		return new XMLOutputter().outputString(array);
	}
}
