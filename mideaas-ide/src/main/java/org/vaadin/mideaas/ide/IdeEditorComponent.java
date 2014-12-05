package org.vaadin.mideaas.ide;

import org.vaadin.mideaas.editor.MultiUserEditor;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class IdeEditorComponent extends CustomComponent {

	private final MultiUserEditor editor;
	private final Component below;
	
	public IdeEditorComponent(IdeCustomizer customizer, IdeProject project, IdeDoc doc, IdeUser user) {

		setSizeFull();
		
		editor = new MultiUserEditor(user.getEditorUser(), doc.getDoc(), doc.getAceMode());
		editor.setSizeFull();
		
		below = customizer.getBelowEditorComponent(project, user);

		if (below == null) {
			setCompositionRoot(editor);
		}
		else {
			setCompositionRoot(createSplit());
		}
	}
	
	private Component createSplit() {
		VerticalSplitPanel split = new VerticalSplitPanel();
		split.setSizeFull();
		split.setFirstComponent(editor);
		split.setSecondComponent(below);
		split.setSplitPosition(125, Unit.PIXELS, true);
		return split;
	}

}
