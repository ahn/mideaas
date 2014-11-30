package org.vaadin.mideaas.editor;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public class MainView extends CustomComponent implements View {

	public MainView(MultiUserProject project, IdeUser user) {
		Ide ide = new Ide(project, user);
		ide.setSizeFull();
		this.setSizeFull();
		setCompositionRoot(ide);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		System.out.println("enter MainView");
	}

}
