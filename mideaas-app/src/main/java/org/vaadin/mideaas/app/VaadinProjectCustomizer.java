package org.vaadin.mideaas.app;

import org.vaadin.mideaas.app.guards.CSSGuard;
import org.vaadin.mideaas.app.guards.JavaGuard;
import org.vaadin.mideaas.app.guards.XmlGuard;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.ide.DefaultProjectCustomizer;
import org.vaadin.mideaas.ide.IdeProject;

public class VaadinProjectCustomizer extends DefaultProjectCustomizer {

	@Override
	public Guard getUpwardsGuardFor(String filename) {
		if (filename.endsWith(".css")) {
			return new CSSGuard();
		}
		if (filename.endsWith(".java")) {
			return new JavaGuard();
		}
		if (filename.endsWith(".xml")) {
			return new XmlGuard();
		}
		return super.getUpwardsGuardFor(filename);
	}
	
	@Override
	public AsyncErrorChecker getErrorCheckerFor(String filename, IdeProject project) {
		if (project instanceof VaadinProject && VaadinProject.isJavaFile(filename)) {
			return ((VaadinProject)project).createErrorChecker(filename);
		}
		return null;
	}

	
	
	

}
