package pt.utl.ist.goalengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mockito.AdditionalMatchers;
import org.yawlfoundation.yawl.util.JDOMUtil;

import pt.utl.ist.bw.elements.DataModelInstanceID;
import pt.utl.ist.bw.elements.DataModelURI;
import pt.utl.ist.bw.elements.DataNameURI;
import pt.utl.ist.bw.messages.AddRelationMessage;
import pt.utl.ist.bw.messages.DataInfo;
import pt.utl.ist.bw.messages.EntityInfo;
import pt.utl.ist.bw.utils.Interface_Client;
import pt.utl.ist.bw.utils.PasswordEncryptor;
import pt.utl.ist.bw.utils.StringUtils;

public class GoalEngineDBGateway extends Interface_Client{

	private String backEndURI;
	
	private String engineUser = "goalengine";
	private String enginePassword = "goalenginepass";
	private String sessionHandle = null;
	
	private static Logger _log = Logger.getLogger(GoalEngineDBGateway.class);
	
	public GoalEngineDBGateway(String backEnd) {
		this.backEndURI = backEnd;
	}
	
	public boolean submitData(DataInfo data) throws IOException { //TODO adapt to the goal engine
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
	
	public boolean addData(EntityInfo data, AddRelationMessage relation) throws IOException {
		if(connected()) {
			Map<String, String> params = prepareParamMap("addData", this.sessionHandle);
			params.put("dataModelURI", data.getDataModelURI().toString());
			params.put("instanceID", data.getInstanceID().toString());
			params.put("dataName", data.getDataNameURI().toString());
			if(data instanceof DataInfo) {
				params.put("dataType", ((DataInfo)data).getDataType());
				params.put("isKey", Boolean.toString(((DataInfo)data).isKey()));
			}
			
			if(relation == null) {
				params.put("relation", "<relation/>");
			} else {
				params.put("relation", relation.toXMLString());
			}
			
			String result = executePost(this.backEndURI, params);
			if(successful(result)) {
				return true;
			} else {
				_log.error(result);
			}
		}
		return false;
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
    
    protected ArrayList<DataInfo> xmlToDataInfo(String xml, DataModelURI dataModelURI, DataModelInstanceID instanceID) { //TODO test
		//@see DataRepository.DataReposirotyServerInterface
		ArrayList<DataInfo> allData = new ArrayList<DataInfo>();
		
		if(xml != null && successful(xml)) {
			Document doc = StringUtils.stringToDoc(xml);
			if(doc != null) {
				Element root = doc.getRootElement();
				if(root.getChildren().size() > 1) {
					// element is an entity
					for (Object childEntity : root.getChildren()) {
						Element entityXML = (Element) childEntity;
						for (Object childAtt : entityXML.getChildren()) {
							DataInfo data = new DataInfo();
							Element attXML = (Element) childAtt;
							data.setInstanceID(instanceID);
							data.setDataModelURI(dataModelURI);
							data.setDataNameURI(new DataNameURI(attXML.getName()));
							data.setDataType(attXML.getChildText("Type"));
							data.setValue(attXML.getChildText("Value"));
							data.setRestrictions(null); //FIXME
							data.isSkipped(Boolean.parseBoolean(attXML.getAttributeValue("isSkipped")));
							data.isDefined(Boolean.parseBoolean(attXML.getAttributeValue("isDefined")));
							data.isKey(Boolean.parseBoolean(attXML.getChildText("isKey")));
							allData.add(data);
						}
					}
				} else {
					// element is an attribute
					Element attXML = (Element) root.getChildren().get(0);
					DataInfo data = new DataInfo();
					data.setInstanceID(instanceID);
					data.setDataModelURI(dataModelURI);
					data.setDataNameURI(new DataNameURI(attXML.getName()));
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

    ////////////////////////////////
}
