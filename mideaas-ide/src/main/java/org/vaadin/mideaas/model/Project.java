//package org.vaadin.mideaas.model;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Collection;
//import java.util.List;
//
//import org.vaadin.mideaas.java.util.InMemoryCompiler;
//import org.vaadin.chatbox.SharedChat;
//import org.vaadin.mideaas.frontend.PomXml.Dependency;
//
///**
// * 
// *   src/main/java/org/vaadin/mideaas/MideaasNavigationView.java
// *
// *   src/main/java/{PACKAGE}/App.java
// * 
// * M src/main/java/{PACKAGE}/Main.java
// * M src/main/java/{PACKAGE}/Main.clara.xml
// * 
// *   src/main/java/{PACKAGE}/Another.java
// * M src/main/java/{PACKAGE}/Another.clara.xml
// *
// * M src/main/java/{PACKAGE}/SomeFile.java
// * M src/main/java/{PACKAGE}/SomeFile2.java
// *
// *   src/main/webapp/META-INF/ ???
// *   src/main/webapp/WEB-INF/web.xml
// *
// * M pom.xml
// *
// * 
// * M = Modifiable by users.
// *
// */
//public interface Project {
//
//	public enum ProjectType {
//		vaadin, vaadinAppEngine, vaadinOSGi, python, generic;
//	}
//	
//	public interface ProjectListener {
//		// TODO: maybe multiple methods, not just one all-purpose "changed"
//		public void changed();
//	}
//
//	public String getName();
//
//	public String getPackageName();
//
//	public File getPomXmlFile();
//
//	public boolean containsComponent(String viewName);
//
//	public Collection<String> getComponentNames();
//
//
//	public SharedView getComponent(String viewName);
//
//
//	SharedView createComponent(String name);
//	
//
//	public void addDependency(String xmlSnippet);
//
//
//	public List<Dependency> getDependencies();
//
//	public void writeToDisk() throws IOException;
//
//	public File getProjectDir();
//
////	public String getClassPath();
//	
//	public void addListener(ProjectListener li);
//	public void removeListener(ProjectListener li);
//
//	public InMemoryCompiler getCompiler();
//
//	public SharedChat getChat();
//
//}
