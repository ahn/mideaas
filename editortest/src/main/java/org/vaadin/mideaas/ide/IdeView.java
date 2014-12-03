package org.vaadin.mideaas.ide;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public class IdeView extends CustomComponent implements View {

	private final IdeProject project;
	
	public IdeView(IdeProject project, IdeUser user, IdeCustomizer cust) {
		this.project = project;
		Ide ide = new Ide(project, user, cust);
		ide.setSizeFull();
		setSizeFull();
		setCompositionRoot(ide);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		getUI().getPage().setTitle(project.getName());
	}

}
