package org.vaadin.mideaas.frontend;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import fi.jasoft.qrcode.QRCode;

@SuppressWarnings("serial")
public class CFAppView extends CustomComponent{

	private LogView logView;
	private String id;
	private CFAppsView cfAppsView;
	private final Deployer deployer;

	public CFAppView(final String id, String xml,final CFAppsView cfAppsView, final LogView logView, Deployer deployer) {
		super();
		this.id = id;
		this.logView=logView;
		this.cfAppsView = cfAppsView;
		this.deployer = deployer;
		updateContent(xml);
	}

	private void updateContent(String xml) {
		HorizontalLayout hlo = new HorizontalLayout();
		VerticalLayout vlo = new VerticalLayout();
		Label text = new Label("XML related to application:");
		Label xmllabel = new Label(xml);
		xmllabel.setContentMode(ContentMode.TEXT);
		vlo.addComponent(text);
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
					ClientResponse response = Deployer.startApplication(id);
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
					ClientResponse response = Deployer.stopApplication(id);
					String responseString = response.getEntity(new GenericType<String>(){});
					updateContent(responseString);
				}
			});
			restart.addClickListener(new Button.ClickListener() {				
				@Override
				public void buttonClick(ClickEvent event) {
					ClientResponse response = Deployer.restartApplication(id);
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
		if (xml.contains("STARTED")){
			String uriToService = Deployer.parseUrl(xml);
			Link link = new Link("link to app", new ExternalResource(uriToService));
			QRCode qrCode = new QRCode();
			qrCode.setValue(uriToService);
			qrCode.setWidth("128px");
			qrCode.setHeight("128px");
			vlo.addComponent(qrCode);
			vlo.addComponent(link);
		}
		xmllabel.setValue(xml);
	}	
}
