package org.vaadin.mideaas.frontend;

import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.TextRange;
import org.vaadin.mideaas.editor.CollaborativeAceEditor;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.MultiUserEditor;

import com.vaadin.event.FieldEvents.TextChangeListener;

@SuppressWarnings("serial")
public class XmlMultiUserEditor extends MultiUserEditor {

	public XmlMultiUserEditor(EditorUser user, MultiUserDoc mud) {
		super(user, mud);
	}
	
	@Override
	protected void configureEditor(CollaborativeAceEditor ed) {
		super.configureEditor(ed);
		ed.setMode(AceMode.xml);
	}

	public void setSelectionChangeListener(SelectionChangeListener listener) {
		getCurrentEditor().addSelectionChangeListener(listener);
	}

	public void setTextChangeListener(TextChangeListener listener) {
		getCurrentEditor().addTextChangeListener(listener);
	}

	

	

	
}
