package org.vaadin.mideaas.ide;

import java.io.IOException;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.server.VaadinServlet;

/**
 * Servlet for showing the raw files at URL /raw/PROJECTID/PATH/TO/FILE
 * 
 *
 */
public class IdeServlet extends VaadinServlet {
	private static final long serialVersionUID = 1L;
	
	private final ProjectContainer projects;
	
	public IdeServlet(ProjectContainer projects) {
		this.projects = projects;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if (path != null && path.startsWith("/raw/")) {
			Matcher m = IdeUtil.RE_URL_FILE.matcher(path.substring(5));
			if (!m.matches()) {
				notFound(response, path);
				return;
			}
			String projectId = m.group(1);
			String filename = m.group(2);
			IdeProject p = projects.getProject(projectId);
			if (p == null) {
				notFound(response, projectId);
				return;
			}
			IdeDoc doc = p.getDoc(filename);
			if (doc == null) {
				notFound(response, filename);
				return;
			}
			response.getWriter().write(doc.getText());
		}
		else {
			super.service(request, response);
		}
	}

	private void notFound(HttpServletResponse response, String s) throws IOException {
		response.setStatus(404);
		response.getWriter().write("Not found: " + s);
	}
}
