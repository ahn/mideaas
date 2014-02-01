package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocManager;
import org.vaadin.mideaas.editor.DocManager.DifferingChangedListener;
import org.vaadin.mideaas.editor.ErrorChecker;
import org.vaadin.mideaas.editor.MultiUserEditor;
import org.vaadin.mideaas.java.JavaSyntaxErrorChecker;
import org.vaadin.mideaas.java.util.CompilingService;

public class ProjectFile extends ProjectItem {
	
	private final DocManager KEKE;
	public ProjectFile(String name, String content, ErrorChecker checker, File saveBaseTo, ProjectLog log) {
		super(name);
		KEKE = new DocManager(new AceDoc(content));
	}
	
	public static ProjectFile newJavaFile(String name, String content, File saveBaseTo, ProjectLog log) {
		return new ProjectFile(name, content, new JavaSyntaxErrorChecker(), saveBaseTo, log);
	}
	
	public DocManager getMud() {
		return KEKE;
	}
	
	public MultiUserEditor  createEditor(User user) {
		return createEditor(user, KEKE, getName());
	}
	
	public String getFileEnding() {
		return getFileEnding(getName());
	}
	
	public static MultiUserEditor createEditor(User user, DocManager mud, String name) {
		MultiUserEditor ed = new MultiUserEditor(user.getEditorUser(), mud);
		ed.setTitle(name);
		String ending = getFileEnding(name);
		ed.setMode(AceMode.forFileEnding(ending));
		return ed;
	}
	
	public static String getFileEnding(String name) {
		int i = name.lastIndexOf(".");
		return i==-1 ? "" : name.substring(i+1);
	}
	
	@Override
	public String[] getJavaClass() {
		String cls = getJavaClassName();
		if (cls!=null) {
			String content = getBaseText();
			return new String[] {cls, content};
		}
		return null;
	}
	
	private String getJavaClassName() {
		String name = getName();
		if (name.endsWith(".java")) {
			return name.substring(0,name.length()-".java".length());
		}
		return null;
	}

	@Override
	public void writeBaseToDisk(File src) throws IOException {
		String s = getBaseText();
		FileUtils.write(new File(src, getName()), s);
	}

	@Override
	public void removeFromDir(File sourceDir) {
		new File(sourceDir, getName()).delete();
	}

	@Override
	public void removeFromClasspathOf(CompilingService compiler,
			String packageName) {
		compiler.removeClass(packageName+"."+getJavaClassName());
	}

	@Override
	public void removeUser(User user) {
		getMud().removeUserDoc(user.getEditorUser());
	}

	@Override
	public void addDifferingChangedListener(DifferingChangedListener li) {
		getMud().addDifferingChangedListener(li);
	}
	
	@Override
	public void removeDifferingChangedListener(DifferingChangedListener li) {
		getMud().removeDifferingChangedListener(li);
	}

	public String getBaseText() {
		return getMud().getBase().getDoc().getText(); // XXX Ugly
	}

}
