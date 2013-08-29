package org.vaadin.mideaas.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class XmlRpcServerDetails extends Window {
	
	protected Window newWindow(){
		Window settings = new Window("FNTS Server details");
		
		settings.setWidth("640px");
        settings.setHeight("480px");
        
        VerticalLayout main = new VerticalLayout();
        HorizontalLayout top = new HorizontalLayout();
        
        Panel topPanel = new Panel();
        final com.vaadin.ui.ComboBox servers = new com.vaadin.ui.ComboBox("Servers");
        final com.vaadin.ui.TextField newServer = new com.vaadin.ui.TextField("New FNTS server");
        final com.vaadin.ui.Button btnAdd = new com.vaadin.ui.Button("Add Server", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) { 
            	URL url = null;
            	try {
            	    url = new URL(newServer.getValue());
            	} catch (MalformedURLException e) {
            		Notification.show("Whoops", "The given server URL is malformed", Notification.Type.ERROR_MESSAGE);
            	}
            	if (url != null) {
            		try {
            			servers.addItem(newServer.getValue());
            			servers.setValue(newServer.getValue());
            			newServer.setValue("");
            		}
            		catch (Exception e) {
            			
            		}
            	}
            }});
        final com.vaadin.ui.Button btnRemove = new com.vaadin.ui.Button("Remove Server", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) { 
            	// TODO: a quick check if the user is sure
            	servers.removeItem(servers.getValue());
            }});
        
        top.addComponent(servers);
        top.addComponent(newServer);
        top.addComponent(btnAdd);
        top.addComponent(btnRemove);
        topPanel.setContent(top);
		
		Label label = new Label("mah label!");
		
		main.addComponent(topPanel);
		main.addComponent(label);
		
		settings.setContent(main);
		settings.center();
		
		return settings;
	}
	
}