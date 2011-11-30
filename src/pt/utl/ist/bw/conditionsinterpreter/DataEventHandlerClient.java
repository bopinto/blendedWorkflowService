package pt.utl.ist.bw.conditionsinterpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

import pt.utl.ist.bw.elements.CaseInstanceID;
import pt.utl.ist.bw.elements.CaseURI;
import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;
import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.EntityInfo;
import pt.utl.ist.bw.utils.Interface_Client;
import pt.utl.ist.bw.utils.PasswordEncryptor;
import pt.utl.ist.bw.utils.StringUtils;


/**
 * Submits the new data elements to the Data Repository
 * Handles events of data changes.
 * Uses Interface_Client java class to send the messages though the network.
 * @see Interface_Client
 * @author bernardoopinto
 *
 */
public class DataEventHandlerClient extends Interface_Client {
	
	private String backEndURI;
	
    protected String engineUser = "bw";
    protected String enginePassword = "bwpass";
	private String sessionHandle = null;
	
	private static Logger _log = Logger.getLogger(DataEventHandlerClient.class);
	
	public DataEventHandlerClient(String backEndURI) {
		this.backEndURI = backEndURI;
	}
	
	public DataModelInstanceID createNewModelInstance(DataModelURI dataModelURI, 
			CaseURI yawlSpecURI, CaseInstanceID yawlID, CaseURI goalSpecURI, CaseInstanceID goalID) throws IOException {
		DataModelInstanceID instanceID = null;
		if(connected()) {
			Map<String, String> params = prepareParamMap("createNewModelInstance", this.sessionHandle);
			params.put("dataModelURI", dataModelURI.toString());
			params.put("yawlSpecURI", yawlSpecURI.toString());
			params.put("yawlInstanceID", yawlID.toString());
			params.put("goalSpecURI", goalSpecURI.toString());
			params.put("goalInstanceID", goalID.toString());
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				instanceID = new DataModelInstanceID(result);				
			} else {
				_log.error(result);
			}
		}
		return instanceID;
	}
	/**
	 * Gets the information about an element.
	 * @param elementURI the element URI (if it is an Entity, it is only [Entity name]; if it is an attribute, it should be [entity name].[attribute name])
	 * @param instanceID the instance id of the data model
	 * @return the data information or null
	 * @throws IOException if you can't connect with the data repository
	 */
	public ArrayList<DataInfo> getElementDataInfo(DataModelURI dataModelURI, DataNameURI elementURI, DataModelInstanceID instanceID) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("getElementData", this.sessionHandle);
			params.put("dataModelURI", dataModelURI.toString());
			params.put("elemURI", elementURI.toString());
			params.put("instanceID", instanceID.toString());

			return xmlToDataInfo(executePost(this.backEndURI, params), dataModelURI, instanceID);
		}
		return new ArrayList<DataInfo>();
	}
	
	public String getParameterMapping(DataModelURI dataModelURI, String parameter) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("getParameterMapping", this.sessionHandle);
			params.put("dataModelURI", dataModelURI.toString());
			params.put("parameter", parameter);
			
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return result;
			} else {
				_log.error(result);
			}
		}
		return null;
	}
	
	public EntityInfo getEntityInfo(DataModelURI dataModelURI, DataNameURI elementURI, DataModelInstanceID instanceID) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("getElementData", this.sessionHandle);
			params.put("dataModelURI", dataModelURI.toString());
			params.put("elemURI", elementURI.toString());
			params.put("instanceID", instanceID.toString());
			return xmlToEntityInfo(executePost(this.backEndURI, params), dataModelURI, instanceID);
		}
		return null;
	}
	
	public boolean skipData(String dataModelURI, String instanceID, String elementURI) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("skipData", this.sessionHandle);
			params.put("dataModelURI", dataModelURI);
			params.put("instanceID", instanceID);
			params.put("elementURI", elementURI);
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return true;
			} else {
				_log.error(result);
			}
		}
		return false;
	}
	
	public boolean submitData(DataInfo data) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("submitData", this.sessionHandle);
			params.put("dataModelURI", data.getDataModelURI().toString());
			params.put("instanceID", data.getInstanceID().toString());
			params.put("dataName", data.getDataNameURI().toString());
			params.put("dataType", data.getDataType());
			params.put("value", data.getValue());
			params.put("restrictions", data.getRestrictions());
			params.put("isSkipped", Boolean.toString(data.isSkipped()));
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return true;
			} else {
				_log.error(result);
			}
		}
		
		return false;
	}
	
	// returns data model in xml
	public String getDataModel(DataModelURI dataModelURI, DataModelInstanceID instanceID) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("getDataModel", this.sessionHandle);
			params.put("dataModelURI", dataModelURI.toString());
			params.put("instanceID", instanceID.toString());
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return result;
			} else { 
				_log.error(result);
			}
		}
		return null;
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
    
    protected boolean elementIsEntity(String elementURI) {
    	String[] elemSplitted = elementURI.split("\\.");
    	return (elemSplitted.length == 2);
    }
    
	protected ArrayList<DataInfo> xmlToDataInfo(String xml, DataModelURI dataModelURI, DataModelInstanceID instanceID) { //TODO test
		//@see DataRepository.DataReposirotyServerInterface
		ArrayList<DataInfo> allData = new ArrayList<DataInfo>();
		
		if(xml != null && successful(xml)) {
			Document doc = StringUtils.stringToDoc(xml);
			if(doc != null) {
				Element root = doc.getRootElement(); // get "Element"
				// get the entities
				for (Object childEntity : root.getChildren("Entity")) {
					Element entityXML = (Element) childEntity;
					for (Object childAtt : entityXML.getChildren("Attribute")) {
						DataInfo data = new DataInfo();
						Element attXML = (Element) childAtt;
						data.setInstanceID(instanceID);
						data.setDataModelURI(dataModelURI);
						data.setDataNameURI(new DataNameURI(attXML.getChildText("Name")));
						data.setDataType(attXML.getChildText("Type"));
						data.setValue(attXML.getChildText("Value"));
						data.setRestrictions(null); //FIXME
						data.isSkipped(Boolean.parseBoolean(attXML.getAttributeValue("isSkipped")));
						data.isDefined(Boolean.parseBoolean(attXML.getAttributeValue("isDefined")));
						data.isKey(Boolean.parseBoolean(attXML.getChildText("isKey")));
						allData.add(data);
					}
				}
				for (Object childAttribute : root.getChildren("Attribute")) {
					// get the attributes
					Element attXML = (Element) childAttribute;
					DataInfo data = new DataInfo();
					data.setInstanceID(instanceID);
					data.setDataModelURI(dataModelURI);
					data.setDataNameURI(new DataNameURI(attXML.getChildText("Name")));
					data.setDataType(attXML.getChildText("Type"));
					data.setValue(attXML.getChildText("Value"));
					data.setRestrictions(null); //FIXME
					data.isSkipped(Boolean.parseBoolean(attXML.getAttributeValue("isSkipped")));
					data.isDefined(Boolean.parseBoolean(attXML.getAttributeValue("isDefined")));
					data.isKey(Boolean.parseBoolean(attXML.getChildText("isKey")));
					allData.add(data);
				}
				return allData;
			}
		}
		return null;
	}
	
	protected EntityInfo xmlToEntityInfo(String xml, DataModelURI dataModelURI, DataModelInstanceID instanceID) {
		EntityInfo entityInfo = null;
		if(xml != null && successful(xml)) {
			Document doc = StringUtils.stringToDoc(xml);
			if(doc == null) {
				return null;
			}
				
			Element root = doc.getRootElement(); // get "Element"
			// get the entities
			Element entityXML = (Element) root.getChild("Entity");
			entityInfo = new EntityInfo();
			entityInfo.setInstanceID(instanceID);
			entityInfo.setDataModelURI(dataModelURI);
			entityInfo.setDataNameURI(new DataNameURI(entityXML.getChildText("Name")));
			entityInfo.isDefined(Boolean.parseBoolean(entityXML.getAttributeValue("isDefined")));
			entityInfo.isSkipped(Boolean.parseBoolean(entityXML.getAttributeValue("isSkipped")));
		}
		return entityInfo;
	}

    ////////////////////////////////
}
