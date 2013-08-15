package org.vaadin.mideaas.app;

import org.vaadin.mideaas.model.ExperimentUser;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class ExperimentLoginPanel extends Panel {

	private final MideaasUI ui;

	public ExperimentLoginPanel(MideaasUI ui) {
		super("Login");
		this.ui = ui;
	}
	
	@Override
	public void attach() {
		super.attach();
		
		createLayout();
		setSizeFull();
	}

	private void createLayout() {
		HorizontalLayout ho = new HorizontalLayout();
		final TextField nickField = new TextField("Nick (shown to others)");
		final TextField codeField = new TextField("Anonymizer code (not shown)");
		Button button = new Button("Login");
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String nick = nickField.getValue();
				String code = codeField.getValue();
				if (nick.isEmpty() || code.isEmpty()) {
					Notification.show("Please fill both fields.");
				}
				else {
					ui.loggedIn(ExperimentUser.newUser(nick, code));
				}
			}
		});
		ho.addComponent(nickField);
		ho.addComponent(codeField);
		ho.addComponent(button);
		ho.setSpacing(true);
		ho.setComponentAlignment(button, Alignment.BOTTOM_CENTER);
		setContent(ho);
		ho.setMargin(true);
		nickField.focus(); // ?
	}

	
}
