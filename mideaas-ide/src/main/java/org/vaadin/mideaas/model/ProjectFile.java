package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.JavaSyntaxGuard;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;
import org.vaadin.mideaas.editor.MultiUserEditor;
import org.vaadin.mideaas.editor.RemoveErrorsFilter;
import org.vaadin.mideaas.editor.XmlSyntaxGuard;
import org.vaadin.mideaas.frontend.Icons;
import org.vaadin.mideaas.frontend.JavaMultiUserEditor;
import org.vaadin.mideaas.frontend.Util;
import org.vaadin.mideaas.frontend.XmlMultiUserEditor;
import org.vaadin.mideaas.java.util.CompilingService;
import org.vaadin.mideaas.java.util.InMemoryCompiler;

import com.vaadin.server.Resource;

public class ProjectFile extends ProjectItem {
	
	private final MultiUserDoc mud;
	private final SharedProject project;
	
	private static final Filter errorRemover = new RemoveErrorsFilter();

	public ProjectFile(SharedProject project, String name, String content) {
		super(name);
		this.project = project;
		mud = new MultiUserDoc(new AceDoc(content), errorRemover, guardForName(name), null, Util.checkerForName(project, name));
	}

	public MultiUserDoc getMud() {
		return mud;
	}
	
	public String getFileEnding() {
		return getFileEnding(getName());
	}
	
	public MultiUserEditor createEditor(User user) {
		EditorUser eu = user.getEditorUser();
		String name = getName();
		if (name.endsWith(".java")) {
			String cls = project.getPackageName()+"."+name.substring(0, name.length()-5);
			InMemoryCompiler compiler = project.getCompiler().getInMemoryCompiler();
			return new JavaMultiUserEditor(eu, mud, compiler, cls);
		}
		else if (name.endsWith(".xml")) {
			return new XmlMultiUserEditor(eu, mud);
		}
		else {
			return new MultiUserEditor(eu, mud);
		}
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
	public void addUser(User user) {
		getMud().createChildDoc(user.getEditorUser());
	}

	@Override
	public void removeUser(User user) {
		getMud().removeChildDoc(user.getEditorUser());
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
