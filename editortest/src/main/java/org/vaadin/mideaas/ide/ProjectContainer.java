package org.vaadin.mideaas.ide;

import java.util.HashMap;

public class ProjectContainer {

	private static HashMap<String, IdeProject> projects = new HashMap<String, IdeProject>();
	
	public ProjectContainer() {
		
	}
	
	public synchronized IdeProject getProject(String name) {
		return projects.get(name);
	}
	
	public synchronized IdeProject putProject(String name, IdeProject project) {
		return projects.put(name, project);
	}
	
	public synchronized IdeProject removeProject(String name) {
		return projects.remove(name);
	}
	
}
