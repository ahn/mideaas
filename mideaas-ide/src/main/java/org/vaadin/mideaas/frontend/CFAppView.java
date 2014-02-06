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

		//if application is running
		if (xml.contains("STARTED")||xml.contains("STOPPED")){
			Button start=new Button("Start");
			Button stop=new Button("Stop");
			Button restart=new Button("Restart");
			Button delete=new Button("Delete");
			String uriToService = Deployer.parseUrl(xml);
			Link link = new Link("link to app", new ExternalResource(uriToService));
			QRCode qrCode = new QRCode();
			qrCode.setValue(uriToService);
			qrCode.setWidth("128px");
			qrCode.setHeight("128px");
			hlo.addComponent(start);
			hlo.addComponent(stop);
			hlo.addComponent(restart);
			hlo.addComponent(delete);
			vlo.addComponent(qrCode);
			vlo.addComponent(link);
			//button listeners
			start.addClickListener(new Button.ClickListener() {				
				@Override
				public void buttonClick(ClickEvent event) {
					ClientResponse response = Deployer.startApplication(id);
					String responseString = response.getEntity(new GenericType<String>(){});
					updateContent(responseString);
				}
			});
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
		xmllabel.setValue(xml);
	}	
}
