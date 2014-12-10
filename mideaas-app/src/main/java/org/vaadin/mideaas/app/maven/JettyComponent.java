package org.vaadin.mideaas.app.maven;

import java.net.URI;

import org.vaadin.mideaas.app.maven.JettyServer.JettyServerListener;
import org.vaadin.mideaas.app.maven.JettyServer.JettyStatus;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fi.jasoft.qrcode.QRCode;

@SuppressWarnings("serial")
public class JettyComponent extends CustomComponent implements JettyServerListener {

	

	private final JettyServer server;
	
	// TODO: common log for all
	// Now, only the UI that started the build sees it.
	private final LogView logView = new LogView();

	private VerticalLayout layout = new VerticalLayout();

	final private Button startButton = new Button("Start Jetty");
	final private Button stopButton = new Button("Stop Jetty");
	
	final private Button showLogButton = new ShowLogButton("Jetty Log", logView);
	private Label statusLabel = new Label();
	private Link link = new Link("Link To App", null);

	private QRCode qrCode = new QRCode();

	public JettyComponent(JettyServer server) {
		super();
		this.server = server;
		Panel p = new Panel("Jetty Server");
		p.setContent(layout);
		layout.setMargin(true);
		setCompositionRoot(p);
	}

	
	
	private void drawServerStatus(JettyStatus status) {
		if (status.status == JettyServer.Status.STOPPED) {
			drawServerStopped();
		}
		else if (status.status == JettyServer.Status.RUNNING) {
			drawServerRunning(status);
		}
	}

	private void drawServerStopped() {
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		statusLabel.setValue(null);
		link.setVisible(false);
		qrCode.setVisible(false);
	}

	private void drawServerRunning(JettyStatus status) {
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		statusLabel.setValue("Running");
		
		String url = getServerUrl() + ":" + status.port + server.getContextPath() + "/";
		link.setResource(new ExternalResource(url));
		link.setTargetName("_blank");
		link.setVisible(true);
		
		qrCode.setValue(url);
		qrCode.setVisible(true);
	}

	@Override
	public void attach() {
		super.attach();
		buildLayout();
		drawServerStatus(server.getStatus());
		server.addListener(this);
	}
	
	@Override
	public void detach() {
		super.detach();
		
		server.removeListener(this);
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

		this.startButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				logView.clear();
				server.start(logView);
			}
		});
	
		stopButton.addClickListener(new Button.ClickListener(){;
			@Override
			public void buttonClick(ClickEvent event) {
				logView.clear();
				server.stop(logView);
			}
		});
	}



	@Override
	public void jettyServerStatusChanged(final JettyStatus status) {
		getUI().access(new Runnable() {
			
			@Override
			public void run() {
				drawServerStatus(status);
			}
		});
	}
	
	private static String getServerUrl() {
		URI loc = Page.getCurrent().getLocation();
		return loc.getHost();
	}
	
}
