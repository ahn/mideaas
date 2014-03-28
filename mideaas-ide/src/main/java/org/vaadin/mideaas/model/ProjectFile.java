package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.editor.JavaSyntaxGuard;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;
import org.vaadin.mideaas.editor.ErrorChecker;
import org.vaadin.mideaas.editor.MultiUserEditor;
import org.vaadin.mideaas.editor.XmlSyntaxGuard;
import org.vaadin.mideaas.frontend.Icons;
import org.vaadin.mideaas.java.JavaSyntaxErrorChecker;
import org.vaadin.mideaas.java.util.CompilingService;

import com.vaadin.server.Resource;

public class ProjectFile extends ProjectItem {
	
	private final MultiUserDoc mud;
	public ProjectFile(String name, String content, ErrorChecker checker, File saveBaseTo, ProjectLog log) {
		super(name);
		mud = new MultiUserDoc(new AceDoc(content), saveBaseTo, guardForName(name));
	}
	
	public static ProjectFile newJavaFile(String name, String content, File saveBaseTo, ProjectLog log) {
		return new ProjectFile(name, content, new JavaSyntaxErrorChecker(), saveBaseTo, log);
	}
	
	public MultiUserDoc getMud() {
		return mud;
	}
	
	public MultiUserEditor  createEditor(User user) {
		return createEditor(user, mud, getName());
	}
	
	public String getFileEnding() {
		return getFileEnding(getName());
	}
	
	public static MultiUserEditor createEditor(User user, MultiUserDoc mud, String name) {
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
	
	public static DocDiffMediator.Guard guardForName(String name) {
		if (name.endsWith(".java")) {
			return new JavaSyntaxGuard();
		}
		else if (name.endsWith(".xml")) {
			return new XmlSyntaxGuard();
		}
		return null;
	}
	
	@Override
	public Resource getIcon() {
		if (getName().endsWith(".java")) {
			return Icons.DOCUMENT_ATTRIBUTE_J;
		}
		else {
			return Icons.DOCUMENT;
		}
	}

}
