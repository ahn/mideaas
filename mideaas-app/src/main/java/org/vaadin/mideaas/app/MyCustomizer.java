package org.vaadin.mideaas.app;

import java.util.Map;

import org.vaadin.mideaas.app.guards.CSSGuard;
import org.vaadin.mideaas.app.java.VaadinProject;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.ide.DefaultProjectCustomizer;
import org.vaadin.mideaas.ide.IdeProject;

public class MyCustomizer extends DefaultProjectCustomizer {

	@Override
	public IdeProject createProject(String name, Map<String, String> files) {
		if (files.containsKey("pom.xml")) {
			return new VaadinProject(name);
		}
		return super.createProject(name, files);
	}
	
	@Override
	public Guard getUpwardsGuardFor(String filename) {
		if (filename.endsWith(".css")) {
			return new CSSGuard();
		}
		return super.getUpwardsGuardFor(filename);
	}

}
