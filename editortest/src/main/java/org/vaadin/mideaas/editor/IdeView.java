package org.vaadin.mideaas.editor;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public class IdeView extends CustomComponent implements View {

	private final MultiUserProject project;
	
	public IdeView(MultiUserProject project, EditorUser user) {
		this.project = project;
		Ide ide = new Ide(project, user);
		ide.setSizeFull();
		setSizeFull();
		setCompositionRoot(ide);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		getUI().getPage().setTitle(project.getName());
	}

}
