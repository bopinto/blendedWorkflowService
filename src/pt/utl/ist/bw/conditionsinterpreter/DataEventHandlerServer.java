package pt.utl.ist.bw.conditionsinterpreter;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pt.utl.ist.bw.utils.ServletUtils;

//this will implement the http servlet
@SuppressWarnings("serial")
public class DataEventHandlerServer extends HttpServlet {
	private static Logger _log = Logger.getLogger(DataEventHandlerServer.class);
	
	private final String OPEN_FAILURE = "<failure><reason>";
    private final String CLOSE_FAILURE = "</reason></failure>";
    private final String SUCCESS = "<success/>";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);                       // all gets redirected as posts
    }
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        ServletUtils.finalizeResponse(outputWriter, output);
    }
	
	private String processPostQuery(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String userID = request.getParameter("userID");
        String password = request.getParameter("password");
     
        try{
        	if(action != null) {
        		if("connect".equals(action)) {
        			int interval = request.getSession().getMaxInactiveInterval();
        			String handle = SessionHandler.get().createNewSession(userID, password, interval);
        			if(handle == null) {
        				msg.append(failMsg("Username or password wrong"));
        			}
        			msg.append(handle);
        		} else if("checkConnection".equals(action)) {
        			if(SessionHandler.get().checkConnection(sessionHandle)) {
        				msg.append(SUCCESS);
        			} else {
        				msg.append(failMsg("Invalid or expired session"));
        			}
        		}
        		else if("notifyDataUpdate".equals(action)) {
        			String dataModelURI = request.getParameter("dataModelURI");
        			String dataModelInstanceID = request.getParameter("dataModelInstanceID");
        			String elementURI = request.getParameter("elementURI");
        			if(ConditionsInterpreter.get().notifyOfDataUpdate(dataModelURI, dataModelInstanceID, elementURI)) {
        				msg.append(SUCCESS);
        			} else {
        				msg.append(failMsg("")); //TODO missing a reason
        			}
        		} else {
        			msg.append("<failure>Invalid action</failure>");
        		}
        	}
        }
        catch (Exception e) {
            _log.error("Exception in Interface B with action: " + action, e);
        }
        if (msg.length() == 0) {
            msg.append("<failure><reason>Invalid action or exception was thrown." +
                       "</reason></failure>");
        }
        return msg.toString();
	}
	
	protected String failMsg(String msg) {
        return OPEN_FAILURE + msg + CLOSE_FAILURE ;
    }

}
