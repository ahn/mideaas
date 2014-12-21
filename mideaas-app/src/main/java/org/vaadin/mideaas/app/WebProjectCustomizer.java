package org.vaadin.mideaas.app;

import org.vaadin.mideaas.app.checkers.CssErrorChecker;
import org.vaadin.mideaas.app.checkers.JavaScriptErrorChecker;
import org.vaadin.mideaas.app.guards.CSSGuard;
import org.vaadin.mideaas.app.guards.HtmlGuard;
import org.vaadin.mideaas.app.guards.JavaScriptGuard;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.ide.DefaultProjectCustomizer;
import org.vaadin.mideaas.ide.IdeProject;

public class WebProjectCustomizer extends DefaultProjectCustomizer {

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

}
