package org.vaadin.mideaas.frontend;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class DeployComponent extends CustomComponent {

	private final SharedProject project;
	private final UserSettings settings;
	private final LogView logView = new LogView();

	private VerticalLayout layout = new VerticalLayout();

	final private Button deployButton = new Button("Deploy to...");
	final private Button killButton = new Button("Stop applications");
	final private Button showButton = new Button("Show applications");

	final private Button showLogButton = new ShowLogButton("Show XML messages", logView);
	private Label statusLabel = new Label();
	private Link link = new Link("Link To App", null);



	public DeployComponent(SharedProject project, UserSettings settings) {
		super();
		this.project = project;
		this.settings=settings;
		Panel p = new Panel("Deploy panel");
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

		layout.addComponent(deployButton);
		layout.addComponent(showButton);
		layout.addComponent(killButton);

		layout.addComponent(statusLabel);
		layout.addComponent(showLogButton);
		
		
		layout.addComponent(link);
		link.setVisible(false);

		this.deployButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				deployUsingPAASAPI();
			}	
		});

		this.killButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				deleteAppsUsingPAASAPI();
			}
		});

		this.showButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				findAppsInPAASAPI();
			}
		});

		
	}

	private void deleteAppsUsingPAASAPI() {
		logView.newLine("Deletes applications running");
		String appsXML = APIClient.deleteApps();
		logView.newLine(appsXML);
	}
	private void findAppsInPAASAPI() {
		logView.newLine("Gets applications running");
        String appsXML = APIClient.findApps();
        logView.newLine(appsXML);
	}	

	private boolean deployUsingPAASAPI() {
		//this is not working yet
		if (settings.compileGae){
			File file = new File(project.getProjectDir().getAbsolutePath(), "hellotest.tar.gz");		
			if (!packageAsTar(file)){
				return false;
			}
			if (!deployTar(file)){
				return false;
			}
		}else{
			//deploys war over cf-api
			String pathToWar = "C:\\Users\\delga\\Documents\\mideaas\\projects\\wartesti\\target-1\\Wartest.war";
	        //deploys app over api
			APIClient client = new APIClient(pathToWar);
	        String pathToService = client.depployApp(logView);			
	        
	        //TODO show path;
	        if (pathToService.length()>0){
	        	return true;
	        }else{
	        	return false;
	        }
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
}
