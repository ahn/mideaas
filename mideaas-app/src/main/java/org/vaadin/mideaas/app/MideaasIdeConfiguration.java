package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.mideaas.app.java.VaadinProject;
import org.vaadin.mideaas.ide.DefaultIdeConfiguration;
import org.vaadin.mideaas.ide.IdeCustomizer;
import org.vaadin.mideaas.ide.IdeProject;
import org.vaadin.mideaas.ide.ProjectCustomizer;
import org.vaadin.mideaas.ide.Util;

public class MideaasIdeConfiguration extends DefaultIdeConfiguration {


	@Override
	public IdeProject createProject(String id, String name, Map<String, String> files) {
		if (files.containsKey("pom.xml")) { // TODO: more detailed check
			try {
				return new VaadinProject(id, name, createProjectDir(files));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not create VaadinProject for the above reason");
			}
		}
		return super.createProject(id, name, files);
	}



	@Override
	public ProjectCustomizer getProjectCustomizer(IdeProject project) {
		if (project instanceof VaadinProject) {
			// ???
		}
		return new MideaasProjectCustomizer();
	}

	@Override
	public IdeCustomizer getIdeCustomizer() {
		return new MideaasIdeCustomizer();
	}	

	
	
	private static File createProjectDir(Map<String, String> files) throws IOException {
		Path path = Files.createTempDirectory("mideaas-vaadin");
		Util.saveFilesToPath(files, path);
		return path.toFile();
	}


	
}
