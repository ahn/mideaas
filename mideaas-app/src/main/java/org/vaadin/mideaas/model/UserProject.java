//package org.vaadin.mideaas.model;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.vaadin.mideaas.java.util.InMemoryCompiler;
//import org.vaadin.chatbox.SharedChat;
//import org.vaadin.mideaas.frontend.MavenTask;
//import org.vaadin.mideaas.frontend.PomXml.Dependency;
//import org.xml.sax.SAXException;
//
//
//public class UserProject implements Project {
//	
//	private final User user;
//	private final SharedProject shared;
//	private File dir;
//	private String classPath;
//	
//	public UserProject(User user, SharedProject shared) throws IOException {
//		this.user = user;
//		this.shared = shared;
//		dir = createTempDir();
//		ProjectFileUtils.createProjectDirs(dir, shared.getPackageName());
//		ProjectFileUtils.writeInitialFilesToDisk(dir, shared.getPackageName());
//		writeToDisk();
//	}
//
//	public User getUser() {
//		return user;
//	}
//
//	@Override
//	public File getProjectDir() {
//		return dir;
//	}
//	
//	@Override
//	public File getPomXmlFile() {
//		// File may not actually exist at this point. That's ok (?)
//		return ProjectFileUtils.getPomXmlFile(dir);
//	}
//
//	@Override
//	public void writeToDisk() throws IOException {
//        shared.writeToDisk(dir);
//    }
//
//	public Collection<String> getComponentNames() {
//		return shared.getComponentNames();
//	}
//
//	@Override
//	public void addDependency(String xmlSnippet) {
//		try {
//			shared.addDependency(xmlSnippet);
//		} catch (ParserConfigurationException | SAXException | IOException e) {
//			e.printStackTrace(); // XXX
//		}
//		
//		classPath = null;
//		
//		// TODO: would just need to write pom.xml
//		writeToDiskNoThrow();
//		
//	}
//
//	private boolean writeToDiskNoThrow() {
//		try {
//			writeToDisk();
//			return true;
//		} catch (IOException e) {
//			System.err.println("WARNING: " + e.getMessage());
//			return false;
//		}
//	}
//
//	@Override
//	public List<Dependency> getDependencies() {
//		return shared.getDependencies();
//	}
//	
//	
//
//	@Override
//	public String getName() {
//		return shared.getName();
//	}
//	
//	@Override
//	public String getPackageName() {
//		return shared.getPackageName();
//	}
//
//	@Override
//	public boolean containsComponent(String viewName) {
//		return shared.containsComponent(viewName);
//	}
//
//	@Override
//	public SharedView getComponent(String viewName) {
//		return shared.getComponent(viewName);
//	}
//	
//	private File createTempDir() throws IOException {
//		return Files.createTempDirectory("mideaas-"+getName()+"-").toFile();
//	}
//
////	@Override
////	public String getClassPath() {
////		if (classPath==null) {
////			classPath = ProjectFileUtils.getClassPath(dir);
////		}
////		return classPath;
////	}
//	
//	public void compile() {
//		if (!writeToDiskNoThrow()) {
//			return;
//		}
//		
//		// TODO ?
//		MavenTask task = new MavenTask(getPomXmlFile(), Collections.singletonList("compile"));
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.submit(task);
//        executor.shutdown();
//	}
//
//	@Override
//	public SharedView createComponent(String name) {
//		return shared.createComponent(name);
//	}
//
//	@Override
//	public void addListener(ProjectListener li) {
//		shared.addListener(li);
//	}
//
//	@Override
//	public void removeListener(ProjectListener li) {
//		shared.removeListener(li);
//		
//	}
//
//	@Override
//	public InMemoryCompiler getCompiler() {
//		return shared.getCompiler();
//	}
//
//	@Override
//	public SharedChat getChat() {
//		return shared.getChat();
//	}
//
//}
