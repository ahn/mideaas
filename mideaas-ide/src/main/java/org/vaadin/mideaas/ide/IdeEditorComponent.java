package org.vaadin.mideaas.ide;

import org.vaadin.aceeditor.Suggester;
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
	private String title;

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
		editor.setTitle(title);
		editor.setSizeFull();
		draw();
	}

	public void setTitle(String title) {
		this.title = title;
		if (editor != null) {
			editor.setTitle(title);
		}
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

	public Component getBelowEditorComponent() {
		return below;
	}





}
