package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;

import org.vaadin.mideaas.java.util.CompilingService;

abstract public class ProjectItem {

	private final String name;
	
	public ProjectItem(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return [classname, classcontent], or null
	 */
	public String[] getJavaClass() {
		return null;
	}

	abstract public void writeBaseToDisk(File src) throws IOException;

	abstract public void removeFromDir(File sourceDir);

	abstract public void removeFromClasspathOf(CompilingService compiler, String packageName);

	abstract public void removeUser(User user);

}
