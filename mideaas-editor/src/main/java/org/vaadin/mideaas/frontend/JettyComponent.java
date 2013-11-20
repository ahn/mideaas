package org.vaadin.mideaas.frontend;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserSettings;

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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

@SuppressWarnings("serial")
public class JettyComponent extends CustomComponent {

	

	private final SharedProject project;
	private final String contextPath;
	private final User user;
	private final UserSettings settings;
	private final LogView logView = new LogView();

	private VerticalLayout layout = new VerticalLayout();
	
	final private Button startButton = new Button("Start Jetty");
	final private Button stopButton = new Button("Stop Jetty");
	final private Button showLogButton = new ShowLogButton("Jetty Log", logView);
	private Label statusLabel = new Label();
	private Link link = new Link("Link To App", null);

	private Integer port = null;
	private QRCode qrCode = new QRCode();



	public JettyComponent(SharedProject project, User user, UserSettings settings) {
		super();
		this.project = project;
		this.user = user;
		this.settings=settings;
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
		if (settings.gaeDeployTurnedOn){
			startButton.setCaption("Deploy to EasiCloud");
			showLogButton.setCaption("Show XML messages");
			layout.addComponent(statusLabel);
			layout.addComponent(showLogButton);
		}else{
			layout.addComponent(stopButton);
			stopButton.setEnabled(false);
			layout.addComponent(statusLabel);
			layout.addComponent(showLogButton);
		}
		
		layout.addComponent(qrCode);
		qrCode.setVisible(false);
		qrCode.setWidth("128px");
		qrCode.setHeight("128px");
		
		layout.addComponent(link);
		link.setVisible(false);

		if (settings.easiCloudsFeaturesTurnedOn){
			this.startButton.addClickListener(new Button.ClickListener() {				
				@Override
				public void buttonClick(ClickEvent event) {
					logView.clear();
					deployUsingPAASAPI();
				}	
			});
	
		}else{
			this.startButton.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					logView.clear();
					startJetty();
				}
			});
	
	
			stopButton.addClickListener(new Button.ClickListener(){;
				@Override
				public void buttonClick(ClickEvent event) {
					logView.clear();
					stopJetty();
				}
			});
		}
	}

	private boolean deployUsingPAASAPI() {
		File file = new File(project.getProjectDir().getAbsolutePath(), "hellotest.tar.gz");		
		if (!packageAsTar(file)){
			return false;
		}
		if (!deployTar(file)){
			return false;
		}
		return true;
	}

	private boolean packageAsTar(File file) {
        String dirPath = project.getProjectDir().getAbsolutePath();
        String tarGzPath = (file).getAbsolutePath();
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        GzipCompressorOutputStream gzOut = null;
        TarArchiveOutputStream tOut = null;
        try{
            System.out.println(new File(".").getAbsolutePath());
        	//TODO:tää
            /*fOut = new FileOutputStream(new File(tarGzPath));
            bOut = new BufferedOutputStream(fOut);
            gzOut = new GzipCompressorOutputStream(bOut);
            tOut = new TarArchiveOutputStream(gzOut);
            addFileToTarGz(tOut, dirPath, "",first);*/
        } finally {
        	//TODO:ja tää
            /*tOut.finish();
            tOut.close();
            gzOut.close();
            bOut.close();
            fOut.close();*/
        }
        return true;
	}
	
	private boolean deployTar(File file) {
      	 String deployLocation = "http://130.230.142.82:8090/upload";
//      	import org.vaadin.cored.APIClient;

      	 //APIClient.formPost(deployLocation, file);
		return true;
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
