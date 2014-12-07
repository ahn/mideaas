package org.vaadin.mideaas.ide;

import org.vaadin.mideaas.editor.MultiUserEditor;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class IdeEditorComponent extends CustomComponent {

	private final MultiUserEditor editor;
	private Component below = null;
	
	public IdeEditorComponent(IdeProject project, IdeDoc doc, IdeUser user) {
		setSizeFull();
		editor = new MultiUserEditor(user.getEditorUser(), doc.getDoc(), doc.getAceMode());
		editor.setSizeFull();
		draw(null, 0);
	}
	
	public void draw(Component belowEditorComponent, int initialHeightPixels) {
		below = belowEditorComponent;
		if (below == null) {
			setCompositionRoot(editor);
		}
		else {
			setCompositionRoot(createSplit(initialHeightPixels));
		}
	}
	
	private Component createSplit(int initialHeightPixels) {
		VerticalSplitPanel split = new VerticalSplitPanel();
		split.setSizeFull();
		split.setFirstComponent(editor);
		split.setSecondComponent(below);
		split.setSplitPosition(initialHeightPixels, Unit.PIXELS, true);
		return split;
	}

}
