package org.vaadin.mideaas.app.java;

import java.io.File;
import java.io.IOException;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.mideaas.app.java.util.CompilingService;
import org.vaadin.mideaas.app.maven.MavenUtil;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.ide.IdeProject;
import org.vaadin.mideaas.ide.IdeProjectSnapshot;


public class VaadinProject extends IdeProject {
	
	private final File dir;
	
	private final CompilingService compiler;

	/**
	 * Storing written files so we can avoid writing files that have not been changed.
	 */
	private IdeProjectSnapshot writtenSnapshot;

	public interface ClasspathListener {
		public void classpathChanged();
	}

	public VaadinProject(String id, String name, File dir) {
		super(id, name);
		this.dir = dir;
		System.out.println("new VaadinProject(" + name + ") -- dir: " + this.dir);
		compiler = new CompilingService(this);
	}

	public String getClassPath() {
		return MavenUtil.getClassPath(dir);
	}

	public void addClasspathListener(ClasspathListener li) {
		// TODO Auto-generated method stub
		
	}

	public synchronized AsyncErrorChecker createErrorChecker(String filename) {
		String pkg = javaFullClassNameFromFilename(filename);
		return new JavaErrorChecker(pkg, compiler);
	}
	
	public Suggester createSuggester(String filename) {
		String pkg = javaFullClassNameFromFilename(filename);
		return new JavaSuggester(compiler.getInMemoryCompiler(), pkg);
	}
	

	
	private static String javaFullClassNameFromFilename(String filename) {
		String s = filename.substring("src/main/java/".length(), filename.length() - ".java".length());
		return s.replace("/", ".");
	}

	public void writeToDisk() throws IOException {
		IdeProjectSnapshot written = getWrittenSnapshot();
		IdeProjectSnapshot snapshot = getSnapshot();
		if (written==null) {
			snapshot.writeToDisk(dir.toPath());
		}
		else {
			snapshot.writeChangedToDisk(dir.toPath(), written);
		}
		setWrittenSnapshot(snapshot);
	}
	
	private synchronized IdeProjectSnapshot getWrittenSnapshot() {
		return writtenSnapshot;
	}
	
	private synchronized void setWrittenSnapshot(IdeProjectSnapshot snapshot) {
		writtenSnapshot = snapshot;
	}

	public File getProjectDir() {
		return dir;
	}

	public File getPomXmlFile() {
		return new File(dir, "pom.xml");
	}

	

}
