package org.vaadin.mideaas.app.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.vaadin.mideaas.app.VaadinProject;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
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
	final com.vaadin.ui.TextArea listEngines = new com.vaadin.ui.TextArea();
	final com.vaadin.ui.TextArea listDetails = new com.vaadin.ui.TextArea();
	
	protected Window newWindow(final VaadinProject project){

		Window settings = new Window("FNTS Server details");
		
		settings.setWidth("640px");
        settings.setHeight("480px");
        
        Panel labelPanel = new Panel();
        HorizontalLayout labelPanelLayout = new HorizontalLayout();
        Label label = new Label("This label should contain instructions of how to use the tool,</br>" +
        		"but instead it contains a boring placeholder!</br></br>" + 
        		"just checking how this works.");
        label.setContentMode(ContentMode.HTML);
        Label gap = new Label("&nbsp;");
        gap.setContentMode(ContentMode.HTML);
        gap.setWidth("15px");
        labelPanelLayout.addComponent(gap);
        labelPanelLayout.addComponent(label);
        labelPanel.setContent(labelPanelLayout);
        
        VerticalLayout main = new VerticalLayout();
        HorizontalLayout top = new HorizontalLayout();
        
        Panel topPanel = new Panel();
        Panel enginePanel = new Panel("Available engines");
        enginePanel.setContent(listEngines);
        
        Label space = new Label("");
        space.setWidth("15px");
        
        Panel detailPanel = new Panel("Server details");
        detailPanel.setContent(listDetails);
        
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
        	
        	listDetails.setReadOnly(false);
        	listDetails.setValue(ServerContainer.getServer((String)cmbServers.getValue()).getDetails());
            listDetails.setReadOnly(true);
            
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
            				Map<String, String> result = (HashMap<String, String>)XmlRpcContact.getServerDetails(newServer.getValue(), "details");
            				ServerContainer.addServer(newServer.getValue(), Arrays.asList(result.get("engines").split(" ")), result.get("details"), project);
            				listEngines.setReadOnly(false);
        					listEngines.setValue("");
        					for (String engine : ServerContainer.getServerEngines((String)cmbServers.getValue())){
        						listEngines.setValue(listEngines.getValue() + engine.trim() + "\n");
        					}
        					listEngines.setReadOnly(true);
        					listDetails.setReadOnly(false);
        					listDetails.setValue(ServerContainer.getServer((String)cmbServers.getValue()).getDetails());
        					listDetails.setReadOnly(true);
            				newServer.setValue("");
            				Notification.show("Server saved!", Notification.Type.HUMANIZED_MESSAGE);
            			}
            			catch (NullPointerException e) {
            				Notification.show("Whoops", "Something went wrong while adding a new server", Notification.Type.ERROR_MESSAGE);
            				e.printStackTrace();
            				if (cmbServers.containsId(newServer.getValue())) {
            					cmbServers.removeItem(newServer.getValue());
            				}
            				if (ServerContainer.getServer(newServer.getValue()) != null) {
            					ServerContainer.removeServer(newServer.getValue(), project);
            				}
            				newServer.setValue("");
            				listEngines.setReadOnly(true);
            				listDetails.setReadOnly(true);
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
            		ServerContainer.removeServer((String)cmbServers.getValue(), project);
            		cmbServers.removeItem(cmbServers.getValue());
            	} catch (Exception e) {
            		Notification.show("Whoops", "Something went wrong while removing server", Notification.Type.ERROR_MESSAGE);
        			e.printStackTrace();
            	}
            }});
        
        final com.vaadin.ui.Button btnRefresh = new com.vaadin.ui.Button("Refresh info", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) { 
            	try {
    				Map<String, String> result = (HashMap<String, String>)XmlRpcContact.getServerDetails((String)cmbServers.getValue(), "details");
    				System.out.println(result.values());
    				System.out.println(result.get("details"));
    				ServerContainer.updateServerdata((String)cmbServers.getValue(), Arrays.asList(result.get("engines").split(" ")), result.get("details"), project);
    				listEngines.setReadOnly(false);
					listEngines.setValue("");
					for (String engine : ServerContainer.getServerEngines((String)cmbServers.getValue())){
						listEngines.setValue(listEngines.getValue() + engine.trim() + "\n");
					}
					listEngines.setReadOnly(true);
					listDetails.setReadOnly(false);
        			listDetails.setValue(ServerContainer.getServer((String)cmbServers.getValue()).getDetails());
					listDetails.setReadOnly(true);
            	} catch (Exception e) {
            		Notification.show("Whoops", "Something went wrong while refreshing server data", Notification.Type.ERROR_MESSAGE);
        			e.printStackTrace();
            	}
            }});
        
        Panel serverDetails = new Panel();
        HorizontalLayout detailSection = new HorizontalLayout();
        detailSection.setMargin(true);
        
        
        listEngines.setRows(10);
        listEngines.setColumns(24);
        listEngines.setReadOnly(true);
        
        listDetails.setReadOnly(true);
        listDetails.setRows(10);
        listDetails.setColumns(24);
        
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
					listDetails.setReadOnly(false);
					listDetails.setValue(ServerContainer.getServer((String)cmbServers.getValue()).getDetails());
					listDetails.setReadOnly(true);
				} catch (NullPointerException e) {
					listEngines.setReadOnly(true);
				}
			}
		});
        
        detailSection.addComponent(enginePanel);
        detailSection.addComponent(space);
        detailSection.addComponent(detailPanel);
        
        Label topgap = new Label("&nbsp;");
        topgap.setContentMode(ContentMode.HTML);
        topgap.setWidth("15px");
        top.addComponent(topgap);
        top.addComponent(cmbServers);
        top.addComponent(newServer);
        top.addComponent(btnAdd);
        //top.addComponent(btnRemove);	//might not be a good idea to remove servers...
        topPanel.setContent(top);
        
		serverDetails.setContent(detailSection);
		
		main.addComponent(labelPanel);
		main.addComponent(topPanel);
		main.addComponent(serverDetails);
		main.addComponent(btnRefresh);
		
		settings.setContent(main);
		settings.center();
		
		return settings;
	}
	
}