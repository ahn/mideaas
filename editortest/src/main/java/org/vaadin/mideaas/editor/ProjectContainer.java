package org.vaadin.mideaas.editor;

import java.util.HashMap;

public class ProjectContainer {

	private static HashMap<String, MultiUserProject> projects = new HashMap<String, MultiUserProject>();
	
	public ProjectContainer() {
		
	}
	
	public synchronized MultiUserProject getProject(String name) {
		return projects.get(name);
	}
	
	public synchronized MultiUserProject putProject(String name, MultiUserProject project) {
		return projects.put(name, project);
	}
	
	public synchronized MultiUserProject removeProject(String name) {
		return projects.remove(name);
	}
	
}
