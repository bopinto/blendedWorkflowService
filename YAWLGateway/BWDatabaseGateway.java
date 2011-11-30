package org.yawlfoundation.yawl.elements.data.external;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

public class BWDatabaseGateway extends AbstractExternalDBGateway {
	
	private static Logger _log = Logger.getLogger(BWDatabaseGateway.class);
	
	private DataRepositoryGateway dataRepositoryGateway = new DataRepositoryGateway("http://localhost:8080/dataRepository/yawl");

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element populateTaskParameter(YTask task, YParameter param,
			Element caseData) {
		
		_log.info("Populating task parameter " + param.getName());
		
		String parameterName = param.getName();
		String yawlCaseURI = task.getNet().getSpecification().getSpecificationID().getUri();
		String instanceID = null;
		List<YIdentifier> list = task.getMIActive().getIdentifiers(); // should have 1 identifier
		if (! list.isEmpty()) {  
			YIdentifier itemID = list.get(0);
			YIdentifier caseID = itemID.getRootAncestor();
			instanceID = caseID.get_idString();
		} else {
			_log.error("Could not find active items for task.");
			return null;
		}
		if(instanceID == null) {
			_log.error("Could not find the instance ID.");
			return null;
		}
		try {
			Element el = dataRepositoryGateway.getElementInfo(yawlCaseURI, 
					instanceID, parameterName);
			if(el == null) {
				_log.error("Could not retreive the information about " + parameterName);
			} else {
				return el;
			}
		} catch(IOException ioe) {
			_log.error("Could not contact with the data repository.");
		}
		return null;
	}

	@Override
	public void updateFromTaskCompletion(String paramName, Element outputData,
			Element caseData) {
		_log.info("Updating task parameter " + paramName);
		
		try {
			if(!dataRepositoryGateway.setElement(paramName, outputData)) {
				_log.error("Data not submitted correctly");
			}
		} catch (IOException e) {
			_log.error("Could not send the information to the Data Repository");
		}
	}

	@Override
	public Element populateCaseData(YSpecificationID specID, String caseID,
			List<YParameter> inputParams, List<YVariable> localVars,
			Element caseDataTemplate) {
		
		// register
		String caseURI = specID.getUri();
		String caseUUID = specID.getIdentifier();
		ArrayList<String> vars = new ArrayList<String>();
		for (YParameter yParameter : inputParams) {
			vars.add(yParameter.getName());
		}
		try {
			if(!dataRepositoryGateway.registerYAWLActiveCase(caseURI, caseID, caseUUID, vars)) {
				_log.error("Could not register the case");
			}
		} catch (IOException e) {
			_log.error("Could not send the information to the Data Repository");
		}
		
		return null;
	}

	@Override
	public void updateFromCaseData(YSpecificationID specID, String caseID,
			List<YParameter> outputParams, Element updatingData) {
		//unregister
		String caseUUID = specID.getIdentifier();
		try {
			if(!dataRepositoryGateway.unregisterYAWLActiveCase(caseUUID, caseID)) {
				_log.error("Could not unregister the case");
			}
		} catch (IOException e) {
			_log.error("Could not send the information to the Data Repository");
		}
	}

}
