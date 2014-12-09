package org.vaadin.mideaas.ide;

import java.util.Collections;
import java.util.List;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.aceeditor.Suggestion;
import org.vaadin.mideaas.editor.MultiUserEditor;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class IdeEditorComponent extends CustomComponent {

	private MultiUserEditor editor;
	private Component below = null;
	private VerticalSplitPanel split;
	private int belowHeight = 150;
	
	public IdeEditorComponent() {
		setSizeFull();
		removeEditor();
	}
	
	public IdeEditorComponent(IdeDoc doc, IdeUser user, Suggester suggester) {
		setSizeFull();
		setEditor(doc, user, suggester);
		
	}
	
	public void setEditor(IdeDoc doc, IdeUser user, Suggester suggester) {
		editor = new MultiUserEditor(user.getEditorUser(), doc.getDoc(), doc.getAceMode(), suggester);
		editor.setSizeFull();
		draw();
	}
	
	public void removeEditor() {
		editor = null;
		draw();
	}
	
	public void setBelowEditorComponent(Component belowEditorComponent, int initialHeightPixels) {
		below = belowEditorComponent;
		belowHeight = initialHeightPixels;
		if (split != null) {
			split.setSplitPosition(belowHeight, Unit.PIXELS, true);
		}
		draw();
	}
	
	public void setBelowEditorComponent(Component belowEditorComponent) {
		below = belowEditorComponent;
		draw();
	}
	
	private void draw() {
		if (below == null) {
			setCompositionRoot(getFirstComponent());
		}
		else {
			drawSplit();
		}
	}
	
	private Component getFirstComponent() {
		return editor != null ? editor : new VerticalLayout(); // ?
	}
	
	private void drawSplit() {
		setCompositionRoot(null); // to avoid adding the editorView twice
		if (split == null) {
			split = new VerticalSplitPanel();
			split.setSizeFull();
			split.setSplitPosition(belowHeight, Unit.PIXELS, true);
		}
		split.setFirstComponent(getFirstComponent());
		split.setSecondComponent(below);
		setCompositionRoot(split);
	}

	

}
