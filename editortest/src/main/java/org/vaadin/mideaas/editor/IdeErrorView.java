package org.vaadin.mideaas.editor;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class IdeErrorView extends CustomComponent implements View {

	@Override
	public void enter(ViewChangeEvent event) {
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		this.setSizeFull();
		
		Panel panel = new Panel();
		panel.setCaption("404 Not found");
		panel.setWidth("400px");
		panel.setHeight("400px");
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		panel.setContent(layout);
		
		la.addComponent(panel);
		la.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);

		layout.addComponent(new Label("The link leads nowhere. Maybe the project doesn't exist or has expired."));
		
		layout.addComponent(new Link("Back to frontpage", new ExternalResource("/")));
		
		setCompositionRoot(la);
	}

}
