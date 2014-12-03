package org.vaadin.mideaas.ide;

import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;

import fi.jasoft.qrcode.QRCode;

@SuppressWarnings("serial")
public class CFAppView extends CustomComponent{

	private LogView logView;
	private String id;
	private CFAppsView cfAppsView;
	private final Deployer deployer;
	private String responseString = null;
	
	private Link linkDeploymenyComponent;
	private Button linkToAppDeploymenyComponent;

	public CFAppView(final String id, String xml,final CFAppsView cfAppsView, final LogView logView, 
			Deployer deployer, Link link, Button linkToApp) {
		super();
		this.linkDeploymenyComponent = link;
		this.linkToAppDeploymenyComponent = linkToApp;
		this.id = id;
		this.logView=logView;
		this.cfAppsView = cfAppsView;
		this.deployer = deployer;
		this.responseString = xml;
		updateContent(xml);
	}

	private void updateContent(String xml) {
		HorizontalLayout hlo = new HorizontalLayout();
		final VerticalLayout vlo = new VerticalLayout();
		Label xmllabel = new Label(xml);
		xmllabel.setContentMode(ContentMode.TEXT);
		vlo.addComponent(xmllabel);
		vlo.addComponent(hlo);
		this.setCompositionRoot(vlo);

		//if application is stopped
		if (xml.contains("STOPPED")){
			Button start=new Button("Start");
			hlo.addComponent(start);
			start.addClickListener(new Button.ClickListener() {				
				@Override
				public void buttonClick(ClickEvent event) {
					//ClientResponse response = Deployer.startApplication(id);
					ClientResponse response = deployer.startApplication(id);
					String responseString = response.getEntity(new GenericType<String>(){});
					updateContent(responseString);
				}
			});
		}

		//application is running
		if (xml.contains("STARTED")){
			Button stop=new Button("Stop");
			Button restart=new Button("Restart");			
			hlo.addComponent(stop);
			hlo.addComponent(restart);
			stop.addClickListener(new Button.ClickListener() {				
				@Override
				public void buttonClick(ClickEvent event) {
					//ClientResponse response = Deployer.stopApplication(id);
					ClientResponse response = deployer.stopApplication(id);
					String responseString = response.getEntity(new GenericType<String>(){});
					updateContent(responseString);
				}
			});
			restart.addClickListener(new Button.ClickListener() {				
				@Override
				public void buttonClick(ClickEvent event) {
					//ClientResponse response = Deployer.restartApplication(id);
					ClientResponse response = deployer.restartApplication(id);
					String responseString = response.getEntity(new GenericType<String>(){});
					updateContent(responseString);
				}
			});
		}
		
		//application somehow exists
		if (xml.contains("STARTED")||xml.contains("STOPPED")||xml.contains("CREATED")){
			Button delete=new Button("Delete");						
			hlo.addComponent(delete);
			delete.addClickListener(new Button.ClickListener() {				
				@Override
				public void buttonClick(ClickEvent event) {
					logView.newLine("Deletes applications running");
					String response = deployer.deleteApplication(id);
					logView.newLine(response);
					updateContent(response);
					if (cfAppsView!=null){
						cfAppsView.updateView();
					}
				}
			});
		}
			
		//application is running
		if (xml.contains("STARTED")) {
			
			String uriToService = Deployer.parseUrl(xml);
			QRCode qrCode = new QRCode();
			qrCode.setValue(uriToService);
			qrCode.setWidth("128px");
			qrCode.setHeight("128px");
			vlo.addComponent(qrCode);
			
			if( DeployComponent.getUriToService() != null && uriToService.matches(DeployComponent.getUriToService())) {
				
				if(DeployComponent.isDeployed()) {
					
					Link link = new Link("Link To App", new ExternalResource(uriToService));
					link.setTargetName("_blank");
					vlo.addComponent(link);
				}
				else {
	
					final Button linkToApp = new Button("Link To App");
					linkToApp.setStyleName(Reindeer.BUTTON_LINK);
					linkToApp.addClickListener(new Button.ClickListener() {
						
						@Override
						public void buttonClick(ClickEvent event) {
							// TODO Auto-generated method stub
							String uriToService = Deployer.parseUrl(responseString);
							
							if (uriToService != null) {
								
								try {
									
									URL url = new URL(uriToService);
									HttpURLConnection connection = (HttpURLConnection) url.openConnection();
									int returnCode = connection.getResponseCode();
									
									if (returnCode == 200) {
										//UI.getCurrent().getPage().open(uriToService, "Application");
										DeployComponent.setDeployed(true);
										//Notification.show("Your application is deployed. Press the link one more time and enjoy!");
										Notification.show("It takes a little time for your application to be deployed (around one minute)!\n Please try later.");
										linkToAppDeploymenyComponent.setVisible(false);
										linkDeploymenyComponent.setTargetName("_blank");
										linkDeploymenyComponent.setResource(new ExternalResource(uriToService));
										linkDeploymenyComponent.setVisible(true);
										
										linkToApp.setVisible(false);
										Link link = new Link("Link To App", new ExternalResource(uriToService));
										link.setTargetName("_blank");
										vlo.addComponent(link);
									}
									else {
										Notification.show("It takes a little time for your application to be deployed (around one minute)!\n Please try later.");
									}		
								}
								catch (Exception e) {
									System.out.println("Exception thrown! " + e);
								}
							}
						}
					});
					vlo.addComponent(linkToApp);
				}
			}
			else {
				
				Link link = new Link("Link To App", new ExternalResource(uriToService));
				link.setTargetName("_blank");
				vlo.addComponent(link);
			}
		}
		xmllabel.setValue(xml);
	}	
}
