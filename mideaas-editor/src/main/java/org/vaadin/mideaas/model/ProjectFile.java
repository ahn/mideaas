package org.vaadin.mideaas.model;

import java.io.File;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.java.JavaSyntaxErrorChecker;
import org.vaadin.mideaas.frontend.MultiUserEditor;

public class ProjectFile {
	private final String name;
	private final MultiUserDoc mud;
	public ProjectFile(String name, String content, ErrorChecker checker, File saveBaseTo, ProjectLog log) {
		this.name = name;
		mud = new MultiUserDoc(name, new AceDoc(content), checker, saveBaseTo, log);
	}
	
	public static ProjectFile newJavaFile(String name, String content, File saveBaseTo, ProjectLog log) {
		return new ProjectFile(name, content, new JavaSyntaxErrorChecker(), saveBaseTo, log);
	}
	
	public String getName() {
		return name;
	}
	
	public MultiUserDoc getMud() {
		return mud;
	}
	
	public MultiUserEditor createEditor(User user) {
		return createEditor(user, mud, name);
	}
	
	public String getFileEnding() {
		return getFileEnding(name);
	}
	
	public static MultiUserEditor createEditor(User user, MultiUserDoc mud, String name) {
		MultiUserEditor ed = new MultiUserEditor(user, mud);
		String ending = getFileEnding(name);
		ed.setMode(AceMode.forFileEnding(ending));
		return ed;
	}
	
	public static String getFileEnding(String name) {
		int i = name.lastIndexOf(".");
		return i==-1 ? "" : name.substring(i+1);
	}
}
