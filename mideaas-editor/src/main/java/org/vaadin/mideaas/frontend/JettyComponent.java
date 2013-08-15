package org.vaadin.mideaas.frontend;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinServletService;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fi.jasoft.qrcode.QRCode;

@SuppressWarnings("serial")
public class JettyComponent extends CustomComponent {

	

	private final SharedProject project;
	private final String contextPath;
	private final User user;
	private final LogView logView = new LogView();

	private VerticalLayout layout = new VerticalLayout();
	private Button startButton = new Button("Start Jetty");
	private Button stopButton = new Button("Stop Jetty");
	private Button showLogButton = new ShowLogButton("Jetty Log", logView);
	private Label statusLabel = new Label();
	private Link link = new Link("Link To App", null);

	private Integer port = null;
	private QRCode qrCode = new QRCode();



	public JettyComponent(SharedProject project, User user) {
		super();
		this.project = project;
		this.user = user;
		this.contextPath = JettyUtil.contextPathFor(project);
		Panel p = new Panel("Jetty Server");
		p.setContent(layout);
		layout.setMargin(true);
		setCompositionRoot(p);
	}

	@Override
	public void attach() {
		super.attach();

		buildLayout();
	}

	private void buildLayout() {

		layout.addComponent(startButton);
		layout.addComponent(stopButton);
		stopButton.setEnabled(false);
		
		layout.addComponent(statusLabel);
		
		layout.addComponent(showLogButton);
		
		
		layout.addComponent(qrCode);
		qrCode.setVisible(false);
		qrCode.setWidth("128px");
		qrCode.setHeight("128px");
		
		layout.addComponent(link);
		link.setVisible(false);

		startButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				logView.clear();
				startJetty();
			}

		});

		stopButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				logView.clear();
				stopJetty();
			}

		});
	}

	private void startJetty() {
		File pomXml = project.getPomXmlFile();
		
		String target = MavenUtil.targetDirFor(user);
		port = JettyUtil.runJetty(pomXml, contextPath, target, logView);

		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		statusLabel.setValue("Running");
		
		String uri = getServer() + ":" + port + contextPath + "/";
		link.setResource(new ExternalResource(uri));
		link.setVisible(true);
		
		qrCode.setValue(uri);
		qrCode.setVisible(true);
	}
	
	private static String getServer() {
		HttpServletRequest request = VaadinServletService.getCurrentServletRequest();
		return (request.isSecure() ? "https://" : "http://") + request.getServerName();
	}

	private void stopJetty() {
		
		if (port==null) {
			return; // XXX
		}
		

		File pomXml = project.getPomXmlFile();
		
		JettyUtil.stopJetty(port,pomXml,contextPath, logView);
		

		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		statusLabel.setValue(null);
		link.setVisible(false);
		qrCode.setVisible(false);
	}
	
}
