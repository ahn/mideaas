package org.vaadin.mideaas.ide;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;

/*
 * ?getmodelid=MODELID
 *     shows the models xml
 */

@SuppressWarnings("serial")
public class MideaasServlet extends VaadinServlet implements SessionDestroyListener, SessionInitListener {
	
	@Override
	public void servletInitialized() throws ServletException {
		super.servletInitialized();
		//System.out.println("servlet initialized");
		getService().addSessionInitListener(this);
		getService().addSessionDestroyListener(this);
	}
	
	public MideaasServlet() {
		super();
		//System.out.println("Servlet Constructor");
		
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
			//System.out.println("request sent");
			super.service(request, response);
		}
	}

	public void putModelXml(String modelId, String xml) {
		modelXmls.put(modelId, xml);
	}
	
	private String getModelXml(String modelId) {
		return modelXmls.get(modelId);
	}

	@Override
	public void sessionDestroy(SessionDestroyEvent event) {
		// TODO Auto-generated method stub
		//System.out.println("Session Expired");
	}

	@Override
	public void sessionInit(SessionInitEvent event) throws ServiceException {
		// TODO Auto-generated method stub
		//System.out.println("Session created");
	}
    
}
