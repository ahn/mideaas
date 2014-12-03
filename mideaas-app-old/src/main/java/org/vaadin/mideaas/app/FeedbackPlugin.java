package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.vaadin.mideaas.ide.MideaasEditorPlugin;
import org.vaadin.mideaas.ide.model.SharedProject;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FeedbackPlugin implements MideaasEditorPlugin {

	private final File file;
	
	public FeedbackPlugin(File file) {
		this.file = file;
	}

	@SuppressWarnings("serial")
	@Override
	public void extendMenu(MenuBar menuBar, SharedProject project) {
		menuBar.addItem("Feedback", null).addItem("Send feedback", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				openSendWindow();				
			}
			
		});
	}

	@SuppressWarnings("serial")
	private void openSendWindow() {
		final Window w = new Window("Feedback");
		w.setWidth("60%");
		w.setHeight("60%");
		w.center();
		
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		la.setMargin(true);
		w.setContent(la);
		final TextArea area = new TextArea();
		Button bu = new Button("Send");
		bu.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				String s = area.getValue();
				if (s!=null && !s.isEmpty()) {
					send(s, w);
				}
			}
			
		});

		area.setSizeFull();
		la.addComponent(area);
		bu.setWidth("100%");
		la.addComponent(bu);
		la.setExpandRatio(area, 1);
		
		UI.getCurrent().addWindow(w);
		
	}

	private void send(String s, Window closeThisOnSuccess) {
		try {
			FileUtils.write(file, "\n--- FEEDBACK ---\n\n"+s+"\n\n\n", true);
			Notification.show("Thank you!");
			closeThisOnSuccess.close();
		} catch (IOException e) {
			Notification.show("Could not send feedback :( --- " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

}
