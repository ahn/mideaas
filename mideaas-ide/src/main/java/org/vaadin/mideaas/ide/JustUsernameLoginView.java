package org.vaadin.mideaas.ide;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class JustUsernameLoginView extends CustomComponent implements
		IdeLoginView, ClickListener {

	private final VerticalLayout layout = new VerticalLayout();
	private final Panel panel = new Panel();
	private TextField usernameField;

	@Override
	public void enter(ViewChangeEvent event) {
		drawLayout();
		showLogin();
	}

	private void drawLayout() {
		getUI().getPage().setTitle("Welcome");
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		this.setSizeFull();

		panel.setCaption("Welcome");
		panel.setWidth("400px");
		panel.setHeight("400px");

		layout.setMargin(true);
		layout.setSpacing(true);
		panel.setContent(layout);

		la.addComponent(panel);
		la.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);

		setCompositionRoot(la);
	}

	private void showLogin() {

		layout.addComponent(new Label("Enter your name to get started"));

		usernameField = new TextField();
		layout.addComponent(usernameField);

		Button b = new Button("Log in");

		b.addClickListener(this);
		layout.addComponent(b);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		String nick = usernameField.getValue();
		if (nick != null && !nick.isEmpty()) {
			IdeUser user = new IdeUser(IdeUser.randomId(), nick, null);
			((IdeUI) getUI()).logIn(user);
		}
	}

}
