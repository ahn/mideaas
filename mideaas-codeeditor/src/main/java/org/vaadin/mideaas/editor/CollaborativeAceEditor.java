package org.vaadin.mideaas.editor;

import org.vaadin.aceeditor.AceEditor;


@SuppressWarnings("serial")
public class CollaborativeAceEditor extends AceEditor {

	private final SharedDoc sharedText;
	
	public CollaborativeAceEditor(SharedDoc value) {
		super();
		this.sharedText = value;
	}
	
	public SharedDoc getSharedText() {
		return sharedText;
	}
	
	@Override
	public void attach() {
		super.attach();
		sharedText.attachEditor(this);
	}
	
	@Override
	public void detach() {
		super.detach();
		sharedText.detachEditor(this);
	}
	
}
