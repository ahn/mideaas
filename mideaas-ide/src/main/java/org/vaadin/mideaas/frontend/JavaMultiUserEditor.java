package org.vaadin.mideaas.frontend;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.mideaas.editor.CollaborativeAceEditor;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.MultiUserEditor;
import org.vaadin.mideaas.java.JavaSuggester;
import org.vaadin.mideaas.java.util.InMemoryCompiler;

@SuppressWarnings("serial")
public class JavaMultiUserEditor extends MultiUserEditor {

	private InMemoryCompiler compiler;
	private String fullClassName;

	public JavaMultiUserEditor(EditorUser user, MultiUserDoc mud, InMemoryCompiler compiler, String fullClassName) {
		super(user, mud);
		this.compiler = compiler;
		this.fullClassName = fullClassName;
	}
	
	@Override
	protected void configureEditor(CollaborativeAceEditor ed) {
		super.configureEditor(ed);
		ed.setMode(AceMode.java);
		if (!ed.isReadOnly()) {
			JavaSuggester sugger = new JavaSuggester(compiler, fullClassName);
			new SuggestionExtension(sugger).extend(ed);
		}
	}
}
