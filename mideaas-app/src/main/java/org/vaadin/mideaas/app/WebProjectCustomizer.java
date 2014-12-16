package org.vaadin.mideaas.app;

import org.vaadin.mideaas.app.guards.CSSGuard;
import org.vaadin.mideaas.app.guards.HtmlGuard;
import org.vaadin.mideaas.app.guards.JavaScriptGuard;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.ide.DefaultProjectCustomizer;

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

}
