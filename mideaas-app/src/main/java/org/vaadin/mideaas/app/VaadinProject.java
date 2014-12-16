package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.mideaas.app.java.JavaErrorChecker;
import org.vaadin.mideaas.app.java.JavaSuggester;
import org.vaadin.mideaas.app.java.util.CompilingService;
import org.vaadin.mideaas.app.maven.Builder;
import org.vaadin.mideaas.app.maven.JettyServer;
import org.vaadin.mideaas.app.maven.JettyUtil;
import org.vaadin.mideaas.app.maven.MavenUtil;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.ide.IdeDoc;
import org.vaadin.mideaas.ide.IdeProject;
import org.vaadin.mideaas.ide.IdeProjectDir;
import org.vaadin.mideaas.ide.IdeProjectSnapshot;
import org.vaadin.mideaas.ide.IdeUser;


public class VaadinProject extends IdeProject {
		
	private final CompilingService compiler;
	
	private final Builder builder;
	private final JettyServer jettyServer;

	private final IdeProjectDir workDir;
	
	public interface ClasspathListener {
		public void classpathChanged();
	}

	public VaadinProject(String id, String name) throws IOException {
		super(id, name, new VaadinProjectCustomizer());

		workDir = createWorkDir();
		// Have to write the project to disk for compiler and jetty server
		workDir.writeToDisk();
		
		compiler = new CompilingService(this);
		builder = new Builder(this);
		jettyServer = new JettyServer(getPomXmlFile(), JettyUtil.contextPathFor(this));
	}
	
	private IdeProjectDir createWorkDir() throws IOException {
		return new IdeProjectDir(this, Files.createTempDirectory("mideaas-vaadin-").toFile());
	}
	
	public Builder getBuilder() {
		return builder;
	}
	
	public JettyServer getJettyServer() {
		return jettyServer;
	}

	public void addClasspathListener(ClasspathListener li) {
		// TODO!
		
	}

	public synchronized AsyncErrorChecker createErrorChecker(String filename) {
		String pkg = javaFullClassNameFromFilename(filename);
		return new JavaErrorChecker(pkg, compiler);
	}

	private static String javaFullClassNameFromFilename(String filename) {
		String s = filename.substring("src/main/java/".length(), filename.length() - ".java".length());
		return s.replace("/", ".");
	}

	
	public File getWorkDir() {
		return workDir.getDir();
	}

	public File getPomXmlFile() {
		return new File(workDir.getDir(), "pom.xml");
	}

	@Override
	public Suggester createSuggesterFor(String filename, IdeUser user) {
		if (!isJavaFile(filename)) {
			return super.createSuggesterFor(filename, user);
		}
		String pkg = javaFullClassNameFromFilename(filename);
		IdeDoc doc = getDoc(filename);
		if (doc == null) {
			return null;
		}
		
		return new JavaSuggester(pkg, compiler.getInMemoryCompiler(), doc, user.getEditorUser());
	}

	public void refreshClasspath() {
		try {
			writeToDisk();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("WARNING: could not write to disk when refreshing classpath at " + workDir);
			return;
		}
		String cp = MavenUtil.getClassPath(workDir.getDir());
		if (cp == null) {
			System.err.println("WARNING: could not read classpath at " + workDir);
			return;
		}
		compiler.setClassPath(cp);
	}

	public void compileAll() {
		IdeProjectSnapshot snapshot = getSnapshot();
		Map<String, String> classContents = new HashMap<String, String>();

		for (Entry<String, String> e : snapshot.getFiles().entrySet()) {
			if (isJavaFile(e.getKey())) {
				classContents.put(javaFullClassNameFromFilename(e.getKey()), e.getValue());
			}
		}

		compiler.compileAll(classContents);
	}

	
	public static boolean isJavaFile(String filename) {
		return filename.startsWith("src/main/java/") && filename.endsWith(".java");
	}

	public void writeToDisk() throws IOException {
		workDir.writeToDisk();
	}
}
