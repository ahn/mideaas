package org.vaadin.mideaas.ide;

import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.XmlAsyncErrorChecker;
import org.vaadin.mideaas.ide.java.JavaErrorChecker;
import org.vaadin.mideaas.ide.model.SharedProject;

public class Util {
	
	public static AsyncErrorChecker checkerForName(SharedProject project, String name) {
		if (name.endsWith(".java")) {
			String cls = project.getPackageName()+"."+name.substring(0, name.length()-5);
			return new JavaErrorChecker(cls, project.getCompiler());
		}
		else if (name.endsWith(".xml")) {
			return new XmlAsyncErrorChecker();
		}
		return null;
	}

}
