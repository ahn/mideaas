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
		
		settings.setWidth("640px");
        settings.setHeight("480px");
        
        VerticalLayout main = new VerticalLayout();
        HorizontalLayout top = new HorizontalLayout();
        
        Panel topPanel = new Panel();
        
        try {
        	for (Server server : ServerContainer.getServerContainer().getItemIds()) {
        		cmbServers.addItem(server.getIP());
        	}
        
        	String first = ServerContainer.getFirstServer().getIP();
        	cmbServers.setValue(first);
        
        	listEngines.setReadOnly(false);
        	listEngines.setValue("");
        	for (String engine : ServerContainer.getServerEngines(first)){
        		listEngines.setValue(listEngines.getValue() + engine.trim() + "\n");
        	}
        	listEngines.setReadOnly(true);
        } catch (NullPointerException e) {
        	//no servers to connect to, leaving the options empty
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
            		String ping = XmlRpcContact.ping(newServer.getValue());
            		if (ping.equals("pong")) { //ping succeeded!
            			try {
            				cmbServers.addItem(newServer.getValue());
            				cmbServers.setValue(newServer.getValue());
            				Map<String, String> result = (HashMap<String, String>)XmlRpcContact.getServerDetails(newServer.getValue());
            				ServerContainer.addServer(newServer.getValue(), Arrays.asList(result.get("engines").split(" ")));
            				listEngines.setReadOnly(false);
        					listEngines.setValue("");
        					for (String engine : ServerContainer.getServerEngines((String)cmbServers.getValue())){
        						listEngines.setValue(listEngines.getValue() + engine.trim() + "\n");
        					}
        					listEngines.setReadOnly(true);
            				newServer.setValue("");
            				Notification.show("Server saved!", Notification.Type.HUMANIZED_MESSAGE);
            			}
            			catch (NullPointerException e) {
            				Notification.show("Whoops", "Something went wrong while adding new server", Notification.Type.ERROR_MESSAGE);
            				e.printStackTrace();
            				if (cmbServers.containsId(newServer.getValue())) {
            					cmbServers.removeItem(newServer.getValue());
            				}
            				if (ServerContainer.getServer(newServer.getValue()) != null) {
            					ServerContainer.removeServer(newServer.getValue());
            				}
            				newServer.setValue("");
            			}
            		} else {
            			Notification.show("Whoops", "Could not reach the server, check the URL", Notification.Type.ERROR_MESSAGE);
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
        detailSection.setMargin(true);
        
        
        listEngines.setRows(10);
        listEngines.setColumns(25);
        listEngines.setReadOnly(true);
        
        cmbServers.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					listEngines.setReadOnly(false);
					listEngines.setValue("");
					for (String engine : ServerContainer.getServerEngines((String)cmbServers.getValue())){
						listEngines.setValue(listEngines.getValue() + engine + "\n");
					}
					listEngines.commit();
					listEngines.setReadOnly(true);
				} catch (NullPointerException e) {
					listEngines.setReadOnly(true);
				}
			}
		});
        
        detailSection.addComponent(listEngines);
        
        top.addComponent(cmbServers);
        top.addComponent(newServer);
        top.addComponent(btnAdd);
        //top.addComponent(btnRemove);	//might not be a good idea to remove servers...
        topPanel.setContent(top);
		
		Label label = new Label("Server Details");
		
		mainServerDetails.addComponent(label);
		mainServerDetails.addComponent(detailSection);
		serverDetails.setContent(mainServerDetails);
		
		main.addComponent(topPanel);
		main.addComponent(serverDetails);
		main.setMargin(true);
		
		settings.setContent(main);
		settings.center();
		
		return settings;
	}
	
}