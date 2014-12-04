package org.vaadin.mideaas.app;

import org.vaadin.mideaas.app.guards.CSSGuard;
import org.vaadin.mideaas.app.java.VaadinProject;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.ide.DefaultProjectCustomizer;
import org.vaadin.mideaas.ide.IdeProject;

public class MideaasProjectCustomizer extends DefaultProjectCustomizer {

	
	@Override
	public Guard getUpwardsGuardFor(String filename) {
		if (filename.endsWith(".css")) {
			return new CSSGuard();
		}
		return super.getUpwardsGuardFor(filename);
	}
	
	@Override
	public AsyncErrorChecker getErrorCheckerFor(String filename, IdeProject project) {
		System.out.println("getErrorChecker");
		if (project instanceof VaadinProject && filename.endsWith(".java")) {
			System.out.println("getErrorChecker vaadin");
			return ((VaadinProject)project).createErrorChecker(filename);
		}
		return null;
	}
	

}
