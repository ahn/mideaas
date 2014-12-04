package org.vaadin.mideaas.app.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.vaadin.mideaas.app.java.util.CompilingService;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.ide.IdeProject;


public class VaadinProject extends IdeProject {
	
	private final File dir;
	
	private final CompilingService compiler = new CompilingService(this);

	public interface ClasspathListener {
		public void classpathChanged();
	}

	public VaadinProject(String id, String name, File dir) {
		super(id, name);
		this.dir = dir;
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
		return MavenUtil.getClassPath(dir);
	}

	public void addClasspathListener(ClasspathListener li) {
		// TODO Auto-generated method stub
		
	}

	public synchronized AsyncErrorChecker createErrorChecker(String filename) {
		String pkg = javaPackageFromFilename(filename);
		System.out.println("pak! " + pkg);
		return new JavaErrorChecker(pkg, compiler);
	}
	
	private static String javaPackageFromFilename(String filename) {
		System.out.println("pak? " + filename);
		String s = filename.substring("src/main/java/".length(), filename.length() - ".java".length());
		return s.replace("/", ".");
		
	}

}
