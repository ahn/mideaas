package org.vaadin.mideaas.app;

import java.io.IOException;
import java.util.logging.Logger;

import org.vaadin.mideaas.app.checkers.CssErrorChecker;
import org.vaadin.mideaas.app.checkers.JavaScriptErrorChecker;
import org.vaadin.mideaas.app.guards.CSSGuard;
import org.vaadin.mideaas.app.guards.HtmlGuard;
import org.vaadin.mideaas.app.guards.JavaScriptGuard;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.ide.DefaultProjectCustomizer;
import org.vaadin.mideaas.ide.IdeProject;

public class WebProjectCustomizer extends DefaultProjectCustomizer {

	private static final Logger log = Logger.getLogger(WebProjectCustomizer.class.getName());
	
	private static final int CHANGE_SOCKET_PORT = 4321; // ???
	
	private static FileChangeSocketSender sender;
	static {
		try {
			sender = new FileChangeSocketSender("localhost", CHANGE_SOCKET_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			log.warning("Can not send file changes to socket on port "+ CHANGE_SOCKET_PORT);
			sender = null;
		}
	}
		
	private final String projectId;
	
	public WebProjectCustomizer(String projectId) {
		this.projectId = projectId;
	}
	
	@Override
	public Guard getUpwardsGuardFor(String filename) {
		if (filename.endsWith(".css")) {
			return new CSSGuard();
		}
		if (filename.endsWith(".html")) {
			return new HtmlGuard();
		}
		if (filename.endsWith(".js")) {
			return new JavaScriptGuard();
		}
		return super.getUpwardsGuardFor(filename);
	}

	@Override
	public AsyncErrorChecker getErrorCheckerFor(String filename, IdeProject project) {
		if (filename.endsWith(".css")) {
			return new CssErrorChecker();
		}
		else if (filename.endsWith(".js")) {
			return new JavaScriptErrorChecker();
		}
		return super.getErrorCheckerFor(filename, project);
	}

	@Override
	public void docCreated(final String id, MultiUserDoc doc) {
		if (sender != null && (id.endsWith(".html") || id.endsWith(".css"))) {
			sender.follow(projectId+"/"+id, doc.getBase());
		}
	}

}
