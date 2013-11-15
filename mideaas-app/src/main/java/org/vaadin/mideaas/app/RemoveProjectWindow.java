package org.vaadin.mideaas.app;

import org.vaadin.mideaas.frontend.Icons;
import org.vaadin.mideaas.model.LobbyBroadcaster;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class RemoveProjectWindow extends Window {
	
	public RemoveProjectWindow(final User user, final String projectName) {
		super("Remove "+projectName+"?");
		setWidth("300px");
		setHeight("300px");
		center();

		//layout for label and field and horisontal layout
		VerticalLayout ve = new VerticalLayout();
		//layout for confirmation buttons
		HorizontalLayout ho = new HorizontalLayout();
	
		ve.addComponent(new Label("Are you sure you want to permanently remove the project?"));
		final TextField tf = new TextField("Type the project name for confirmation:");
		ve.addComponent(tf);
		Button removeButton = new Button("Remove");
		removeButton.setIcon(Icons.CROSS_SCRIPT);
		removeButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				close();
				if (projectName.equals(tf.getValue())) {
					boolean removed = SharedProject.removeProject(projectName);
					if (removed) {
						SharedProject.removeProject(projectName);
						LobbyView.getLobbyChat().addLine(user.getName() + " removed project " + projectName);
				        LobbyBroadcaster.broadcastProjectsChanged();
					}
					else {
						Notification.show("Failed to remove "+projectName+"!");
					}
				}
				else {
					Notification.show("Incorrect confirmation");
				}
			}
		});
		ho.addComponent(removeButton);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				close();				
			}
		});
		ho.addComponent(cancelButton);
		ve.addComponent(ho);
		this.setContent(ve);
	}

}
