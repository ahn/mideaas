package org.vaadin.mideaas.ide.model;

import java.io.File;
import java.io.IOException;

import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;
import org.vaadin.mideaas.ide.java.util.CompilingService;

import com.vaadin.server.Resource;

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

	abstract public void addUser(User user);
	abstract public void removeUser(User user);

	abstract public Resource getIcon();

	abstract public void addDifferingChangedListener(DifferingChangedListener li);
	abstract public void removeDifferingChangedListener(DifferingChangedListener li);
	
}
