package org.vaadin.mideaas.frontend;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.server.VaadinServlet;

/*
 * ?getmodelid=MODELID
 *     shows the models xml
 */

@SuppressWarnings("serial")
public class MideaasServlet extends VaadinServlet {
	

	public MideaasServlet() {
		super();
		
		// Ugly...
		// Needed for SharedView to call putModelXml when xml changes
		ClaraEditor.setServlet(this);
	}

	private ConcurrentHashMap<String, String> modelXmls =
			new ConcurrentHashMap<String, String>();
	
	@Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("getmodelid") != null) {
			String modelId = request.getParameter("getmodelid");
			String xml = getModelXml(modelId);
			response.setContentType("text/plain");
			if (xml!=null) {
				response.getWriter().print(xml);
				
			}
			else {
				String err = "ERROR: no model with id '"+modelId+"' found.";
				System.err.println(err);
				response.sendError(404, err);
			}
		}
		else {
			super.service(request, response);
		}
	}

	public void putModelXml(String modelId, String xml) {
		modelXmls.put(modelId, xml);
	}
	
	private String getModelXml(String modelId) {
		return modelXmls.get(modelId);
	}
    
}
