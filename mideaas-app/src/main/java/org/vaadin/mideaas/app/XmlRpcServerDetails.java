package org.vaadin.mideaas.app;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Button;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.Notification;

import java.io.*;

import org.vaadin.mideaas.tests.*;
import org.vaadin.mideaas.model.XmlRpcContact;

public class XmlRpcServerDetails extends Window {
	
	List<String> fntsServers = new ArrayList<String>();
	
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
            		Notification.show("Whoops", "A bad server URL was given", Notification.Type.ERROR_MESSAGE);
            	}
            	if (url != null) {
            		fntsServers.add(newServer.getValue());
            		servers.addItem(newServer.getValue());
            		servers.setValue(newServer.getValue());
            		newServer.setValue("");
            	}
            }});
        final com.vaadin.ui.Button btnRemove = new com.vaadin.ui.Button("Remove Server", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) { 
            	// TODO: a quick check if the user is sure
            	fntsServers.remove(servers.getValue());
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
	
	public List<String> getServers() {
		return fntsServers;
	}
}