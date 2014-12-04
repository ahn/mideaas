package org.vaadin.mideaas.app.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.vaadin.mideaas.ide.IdeProject;


public class VaadinProject extends IdeProject {
	
	private final Path dir;

	public interface ClasspathListener {
		public void classpathChanged();
	}

	public VaadinProject(String id, String name) {
		super(id, name);
		this.dir = createDir();
		System.out.println("new VaadinProject(" + name + ") -- dir: " + this.dir);
	}

	private static Path createDir() {
		try {
			return Files.createTempDirectory("mideaas");
		} catch (IOException e) {
			return null;
		}
	}

	public String getClassPath() {
		return ""; // ProjectFileUtils.getClassPath(projectDir);
	}

	public void addClasspathListener(ClasspathListener li) {
		// TODO Auto-generated method stub
		
	}

}
