package org.vaadin.mideaas.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.mideaas.model.ServerContainer;
import org.vaadin.mideaas.model.Server;
import org.vaadin.mideaas.model.XmlRpcContact;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class XmlRpcServerDetails extends Window {
	
	final com.vaadin.ui.ComboBox cmbServers = new com.vaadin.ui.ComboBox("Servers");
    final com.vaadin.ui.TextField newServer = new com.vaadin.ui.TextField("New FNTS server");
	final com.vaadin.ui.TextArea listEngines = new com.vaadin.ui.TextArea("Engines");
	
	protected Window newWindow(){
		Window settings = new Window("FNTS Server details");
		final XmlRpcContact xmlrpc = new XmlRpcContact();
		
		settings.setWidth("640px");
        settings.setHeight("480px");
        
        VerticalLayout main = new VerticalLayout();
        HorizontalLayout top = new HorizontalLayout();
        
        Panel topPanel = new Panel();
        
        for (Server server : ServerContainer.getServerContainer().getItemIds()) {
        	cmbServers.addItem(server.getIP());
        }
        
        
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
            			cmbServers.addItem(newServer.getValue());
            			cmbServers.setValue(newServer.getValue());
            			Map<String, String> result = (HashMap<String, String>)xmlrpc.getServerDetails(newServer.getValue());
            			ServerContainer.addServer(newServer.getValue(), Arrays.asList(result.get("engines").split(" ")));
            			newServer.setValue("");
            		}
            		catch (Exception e) {
            			Notification.show("Whoops", "Something went wrong while adding new server", Notification.Type.ERROR_MESSAGE);
            			e.printStackTrace();
            		}
            	}
            }});
        final com.vaadin.ui.Button btnRemove = new com.vaadin.ui.Button("Remove Server", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) { 
            	// TODO: a quick check if the user is sure
            	try {
            		ServerContainer.removeServer((String)cmbServers.getValue());
            		cmbServers.removeItem(cmbServers.getValue());
            	} catch (Exception e) {
            		Notification.show("Whoops", "Something went wrong while removing server", Notification.Type.ERROR_MESSAGE);
        			e.printStackTrace();
            	}
            }});
        
        Panel serverDetails = new Panel();
        VerticalLayout mainServerDetails = new VerticalLayout();
        HorizontalLayout detailSection = new HorizontalLayout();
        
        
        listEngines.setRows(10);
        listEngines.setColumns(25);
        listEngines.setReadOnly(true);
        
        cmbServers.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				listEngines.setReadOnly(false);
				listEngines.setValue("");
				for (String engine : ServerContainer.getServerEngines((String)cmbServers.getValue())){
					listEngines.setValue(listEngines.getValue() + engine + "\n");
		        }
				listEngines.setReadOnly(true);
			}
		});
        
        detailSection.addComponent(listEngines);
        
        top.addComponent(cmbServers);
        top.addComponent(newServer);
        top.addComponent(btnAdd);
        top.addComponent(btnRemove);
        top.setMargin(true);
        topPanel.setContent(top);
		
		Label label = new Label("Server Details");
		
		mainServerDetails.addComponent(label);
		mainServerDetails.addComponent(detailSection);
		mainServerDetails.setMargin(true);
		serverDetails.setContent(mainServerDetails);
		
		main.addComponent(topPanel);
		main.addComponent(serverDetails);
		
		settings.setContent(main);
		settings.center();
		
		return settings;
	}
	
}