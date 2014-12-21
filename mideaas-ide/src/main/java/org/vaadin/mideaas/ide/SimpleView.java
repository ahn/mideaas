package org.vaadin.mideaas.ide;

import org.vaadin.aceeditor.Suggester;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public class SimpleView extends CustomComponent implements View {

	private final String title;

	public SimpleView(IdeDoc doc, IdeUser user, String title, Suggester suggester) {
		this.title = title;
		IdeEditorComponent ed = new IdeEditorComponent(doc, user, suggester);
		ed.setTitle(title);
		ed.setSizeFull();
		setSizeFull();
		setCompositionRoot(ed);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		getUI().getPage().setTitle(title);
	}

}
