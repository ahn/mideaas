package org.vaadin.mideaas.ide;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.MultiUserEditor;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class SimpleView extends CustomComponent implements View {

	private final String title;
	
	public SimpleView(IdeDoc doc, IdeUser user, String title, Suggester suggester) {
		this.title = title;
		IdeEditorComponent ed = new IdeEditorComponent(doc, user, suggester);
		ed.setSizeFull();
		setCompositionRoot(ed);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		getUI().getPage().setTitle(title);
	}

}
