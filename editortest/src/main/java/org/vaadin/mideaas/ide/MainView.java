package org.vaadin.mideaas.ide;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public class MainView extends CustomComponent implements View {

	public MainView(IdeProject project, IdeUser user) {
		IdeCustomizer cust = ((IdeUI)getUI()).getIdeCustomizer();
		Ide ide = new Ide(project, user, cust);
		ide.setSizeFull();
		this.setSizeFull();
		setCompositionRoot(ide);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		System.out.println("enter MainView");
	}

}
