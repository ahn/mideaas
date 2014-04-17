package org.vaadin.mideaas.frontend;

import org.vaadin.mideaas.model.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.vaadin.mideaas.frontend.Deployer.DeployListener;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserSettings;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import fi.jasoft.qrcode.QRCode;

@SuppressWarnings("serial")
public class DeployComponent extends CustomComponent implements DeployListener, StartOFDeploymentBroadcastListener {

	// IP address of the chosen server (on map application) by user
	private String ipOfPlaceOfDeploy = null;
	
	private final Deployer deployer;
	private final SharedProject project;
	private final UserSettings settings;
	
	private final LogView logView = new LogView();
	private CFAppsView view;

	private UI ui;

	private VerticalLayout layout = new VerticalLayout();
	private VerticalLayout resultLayout = new VerticalLayout();

	private QRCode qrCode = new QRCode();
	private Link linkToApp = new Link("Link To App", null);
	
	// after calling deployApplication Rest method of COAPS API, it takes a little time
	// (I don't know why) for application (written by user) to be deployed. When deployment is done and
	// application is ready to run, this flag will be true.
	private static boolean isDeployed = false;

	// a link button to check whether the application (written by user) is deployed and ready to run or not
	private Button buttonLinkToApp = new Button("Link To App");
	
	// the URL where the application (written by user) is deployed
	private static String uriToService = null;

	final private Button deployButton = new Button("Deploy app");
	private Label paasNameLabel =  new Label();
	//final private Button cancelButton = new Button("Cancel");
	final private Button stopButton = new Button("Delete all apps");
	final private Button showButton = new Button("Show all apps");
	final private Button showLogButton = new ShowLogButton("XMLMessage Log", logView);

	private Embedded loadingImg = new Embedded(null, new ThemeResource(
			"../base/common/img/loading-indicator.gif"));
	protected String responseMessage = null;
	private Button slabutton = null;
	private Button showResponseDataButton = null;
	
	private User user = null;

	public DeployComponent(SharedProject project, UserSettings settings, User user) {
		super();
		
		this.user = user;
		ipOfPlaceOfDeploy = "http://130.230.142.89:8080/CF-api/rest";
		uriToService = null;
		isDeployed = false;
		this.project = project;
		this.settings=settings;
		String pathToWar = new File(project.getTargetPathFor(user).getAbsolutePath(), project.getName()+".war").getAbsolutePath();
		this.deployer = new Deployer(pathToWar, settings.coapsApiUri, this.linkToApp, this.buttonLinkToApp, ipOfPlaceOfDeploy, user, deployButton);
		//System.out.println("ipOfPlaceOfDeploy from DeployComponent: " + deployer.getApiLocation());
	}
	
	public static boolean isDeployed() {
		return isDeployed;
	}

	public static void setDeployed(boolean isDeployed) {
		DeployComponent.isDeployed = isDeployed;
	}

	public static String getUriToService() {
		return uriToService;
	}
	
	private void buildLayout() {
		Panel p = new Panel("CloudFoundry panel");
		p.setContent(layout);
		layout.setSpacing(true);
		
		paasNameLabel.setValue("TAMPERE UNIVERSITY OF TECHNOLOGY");
		layout.addComponent(paasNameLabel);
		
		//TODO: add stuff here
		if (settings.useSlaSelectionMap){
			createSLANavigationFeatures();
		}
		
		layout.addComponent(deployButton);
		deployButton.setDescription("To Tampere University Of Technology");
		
		//layout for showing deployment results
		layout.addComponent(resultLayout);
		loadingImg.setVisible(false);
		layout.addComponent(loadingImg);
		//cancelButton.setVisible(false);
		//layout.addComponent(cancelButton);
		layout.addComponent(showButton);
		layout.addComponent(stopButton);
		layout.addComponent(showLogButton);		
		
		//adds qr code
		qrCode.setVisible(false);
		qrCode.setWidth("128px");
		qrCode.setHeight("128px");
		layout.addComponent(qrCode);

		//link to the deployed application
		linkToApp.setVisible(false);
		layout.setMargin(true);
		layout.addComponent(linkToApp);
		
		// a link button to check whether the application (written by user) is deployed and ready to run or not
		buttonLinkToApp.setVisible(false);
		buttonLinkToApp.setStyleName(Reindeer.BUTTON_LINK);
		layout.addComponent(buttonLinkToApp);
		
		setCompositionRoot(p);
		
		// set the alignment of component to center both vertically and horizontally 
		layout.setComponentAlignment(deployButton, Alignment.MIDDLE_CENTER);
		//layout.setComponentAlignment(cancelButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(showButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(stopButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(showLogButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(loadingImg, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(qrCode, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(linkToApp, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(buttonLinkToApp, Alignment.MIDDLE_CENTER);
		
		//buttonhandlers
		this.deployButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				deploy();
			}	
		});

		/*
		this.cancelButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				cancelDeploy();
				networkingCancelled("Deploy cancelled");
			}	
		});
		 */
		
		this.stopButton.addClickListener(new Button.ClickListener() {				
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
		
		// a link button to check whether the application (written by user) is deployed or not:
		// 1- if not, it notifies user that s/he should wait and try later
		// 2- if yes, make the link button invisible and instead make a link (Vaadin component) 
		// (to the deployed application) visible.
		this.buttonLinkToApp.addClickListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				if (uriToService != null) {
				
					try {
					
						URL url = new URL(uriToService);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						int returnCode = connection.getResponseCode();

						if (returnCode == 200) {
							
							//ui.getPage().open(uriToService, "_blank");
							setDeployed(true);
							//Notification.show("Your application is deployed. Press the link one more time and enjoy!");
							Notification.show("It takes a little time for your application to be deployed (around one minute)!\n Please try later.");
							buttonLinkToApp.setVisible(false);
							linkToApp.setResource(new ExternalResource(uriToService));
							linkToApp.setTargetName("_blank");
				    		linkToApp.setVisible(true);				    		
						}
						else {
							Notification.show("It takes a little time for your application to be deployed (around one minute)!\n Please try later.");
						}	
					}
					catch (Exception e) {}
				}
			}
		});
	}

	private void createSLANavigationFeatures() {
		
		slabutton = new Button("Negotiate SLA");	
		layout.addComponent(slabutton);
		layout.setComponentAlignment(slabutton, Alignment.MIDDLE_CENTER);
		
		showResponseDataButton = new Button("ShowResponseData");
		showResponseDataButton.setEnabled(false);
		layout.addComponent(showResponseDataButton);
		layout.setComponentAlignment(showResponseDataButton, Alignment.MIDDLE_CENTER);
		
		slabutton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				
				// Strange!!! it needs a refresher to synchronize UI
				final Refresher r = new Refresher();
				r.addListener(new Refresher.RefreshListener() {
					
					@Override
					public void refresh(Refresher source) {
						// TODO Auto-generated method stub
						removeExtension(r);
					}
				});
				addExtension(r);
				
				ipOfPlaceOfDeploy = null;
				showResponseDataButton.setEnabled(false);
					
				String params = "/cloud?callbackUri=http://130.230.142.89:8080/mideaastest";
				//String params = "/cloud?callbackUri=http://localhost:8080/mideaas";
				
				
				// opening map application in a sub-window added to current UI
				final Window mapSubWindow = new Window();
				mapSubWindow.setModal(true);
				mapSubWindow.setHeight("90%");
				mapSubWindow.setWidth("70%");
				mapSubWindow.center();
				VerticalLayout mapSubWindowLayout = new VerticalLayout();
				mapSubWindow.setContent(mapSubWindowLayout);
				mapSubWindowLayout.setSizeFull();
				BrowserFrame mapBrowserFrame = new BrowserFrame("",new ExternalResource(settings.slaSelectionMapUri + params));
				mapSubWindowLayout.addComponent(mapBrowserFrame);
				mapBrowserFrame.setSizeFull();
				UI.getCurrent().addWindow(mapSubWindow);
				
				// add a request handler to get callback URL 
				VaadinSession.getCurrent().addRequestHandler(new RequestHandler() {
					
					@Override
					public boolean handleRequest(
							VaadinSession session,
							VaadinRequest request,
							VaadinResponse response) throws IOException {
						// TODO Auto-generated method stub
						VaadinSession.getCurrent().removeRequestHandler(this);
						String paasName = request.getParameter("PaasName");
						ipOfPlaceOfDeploy = request.getParameter("selectedPaas");
						
						if(ipOfPlaceOfDeploy != null && paasName != null) {
							
							//CoapsCaller.setApiLocation(ipOfPlaceOfDeploy + "rest");
							paasNameLabel.setValue(paasName);
							deployer.setApiLocation(ipOfPlaceOfDeploy + "rest");
							deployer.checkIFSomeoneIsDeploying(resultLayout, qrCode, loadingImg);
							showResponseDataButton.setEnabled(true);
							mapSubWindow.close();
							showPlaceOfDeployment(paasName);
						}
						return false;
					}
				});	
			}
		});
		
		//buttonhandlers
		this.showResponseDataButton.addClickListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				
				Notification.show(ipOfPlaceOfDeploy);
			}
		});
	}
	
	private void showPlaceOfDeployment(String paasName) {
		
		 Notification notif = new Notification( paasName, "is chosen", Type.TRAY_NOTIFICATION);
		 notif.setDelayMsec(2000);
		 notif.show(ui.getPage());
		 deployButton.setDescription("To " + paasName);
	}
	
	@Override
	public void attach() {
		
		super.attach();
		buildLayout();
		setUi(UI.getCurrent());
		deployer.addDeployListener(this);
		System.out.println(user.getName() + "add to list");
		StartOfDeploymentBroadcaster.register(this);
		deployer.checkIFSomeoneIsDeploying(resultLayout, qrCode, loadingImg);
	}

	@Override
	public void detach() {
		
		super.detach();
		deployer.removeDeployListener(this);
		System.out.println(user.getName() + "removed from list");
		StartOfDeploymentBroadcaster.unregister(this);
	}

	private synchronized void setUi(UI ui) {
		this.ui = ui;
	}

	private synchronized UI getUi() {
		return ui;	
	}

	public void cancelDeploy() {
		deployer.cancel();
	}
	
	private void deploy() {
		//StartOfDeploymentBroadcaster.unregister(this);
		deployer.deploy(this.settings, this.project, this.logView, this.buttonLinkToApp, this.qrCode);
		//StartOfDeploymentBroadcaster.register(this);
		this.updateCFAppsView();
	}

	private void finishDeploy() {
		loadingImg.setVisible(false);
		deployButton.setEnabled(true);
		//deployButton.setCaption("Deploy");
		//cancelButton.setVisible(false);
	}

	private void deployFail(String s) {
		Label lab = new Label(s);
		lab.setIcon(Icons.CROSS_CIRCLE);
		lab.setSizeUndefined();
		resultLayout.addComponent(lab);
		resultLayout.setComponentAlignment(lab, Alignment.MIDDLE_CENTER);
	}

	private void deploySuccess() {
		Label lab = new Label("Deploy successful");
		lab.setIcon(Icons.TICK_CIRCLE);
		lab.setSizeUndefined();
		resultLayout.addComponent(lab);
		resultLayout.setComponentAlignment(lab, Alignment.MIDDLE_CENTER);
		
	}

	@Override
	public void networkingFinished(final boolean success, final String msg, final String UriToServic) {
		getUi().access(new Runnable() {
			@Override
			public void run() {
				finishDeploy();
				uriToService = UriToServic;
				if (success) {
					deploySuccess();
				} else {
					deployFail(msg);
					qrCode.setVisible(false);
					linkToApp.setVisible(false);
					buttonLinkToApp.setVisible(false);
				}
			}
		});
	}

	@Override
	public void networkingCancelled(final String msg) {
		getUi().access(new Runnable() {
			@Override
			public void run() {
				finishDeploy();
				deployFail(msg);
				qrCode.setVisible(false);
				linkToApp.setVisible(false);
				buttonLinkToApp.setVisible(false);
			}
		});
	}

	@Override
	public void networkingStarted(final String msg) {
		getUi().access(new Runnable() {
			@Override
			public void run() {
				resultLayout.removeAllComponents();
				deployButton.setEnabled(false);
				//cancelButton.setVisible(true);
				loadingImg.setVisible(true);
				loadingImg.setCaption(msg);
				
				linkToApp.setVisible(false);
				buttonLinkToApp.setVisible(false);
				qrCode.setVisible(false);
				setDeployed(false);
				
			}
		});
	}

	//deletes all the environments and applications (mainly because of testing purposes... Should be disabled later)
	private void deleteAppsUsingPAASAPI() {
		logView.newLine("Deletes applications running");
		String xml = deployer.deleteApplications();
		logView.newLine(xml);
		logView.newLine("Deletes environments");
		if(deployer.deleteEnvironments(logView)){
			linkToApp.setVisible(false);
			
			buttonLinkToApp.setVisible(false);
			
			qrCode.setVisible(false);
			deployFail("Apps deleted");
		}
		updateCFAppsView();
	}
	
	private void updateCFAppsView() {
		if (view!=null){
			view.updateView();
		}
	}

	private void findAppsInPAASAPI() {
		logView.newLine("Gets applications running");
        view = new CFAppsView(deployer, logView);
        Window w = new Window("Applications running in CloudFoundry");
		w.center();
		w.setWidth("80%");
		w.setHeight("80%");
		w.setContent(view);
		view.setSizeFull();
		UI.getCurrent().addWindow(w);
	}

	@Override
	public void deployStartted(final String apiLocation, final String appName, final User deployerUser) {

		ui.access(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(deployerUser.getUserId().matches(user.getUserId())) {
					// do nothing
				}
				else if(deployer.getApiLocation().matches(apiLocation) && appName.matches(project.getName()) ) {
					deployButton.setEnabled(false);
					//System.out.println(user.getName() + " start" );
					resultLayout.removeAllComponents();
					buttonLinkToApp.setVisible(false);
					linkToApp.setVisible(false);
					qrCode.setVisible(false);
					loadingImg.setVisible(true);
					loadingImg.setCaption(deployerUser.getName() + " is deploying");
				    Notification notif = new Notification( deployerUser.getName(), Type.TRAY_NOTIFICATION);
				    notif.setDelayMsec(1000);
				    notif.setDescription("started deployment");
					notif.show(ui.getPage());
				}

			}
		});
	}

	@Override
	public void deployFinished(final String apiLocation, final String appName, final User deployerUser) {
		
		ui.access(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(deployerUser.getUserId().matches(user.getUserId())) {
					// do nothing
				}
				else if(deployer.getApiLocation().matches(apiLocation) && appName.matches(project.getName()) ) {
					deployButton.setEnabled(true);
					//System.out.println(user.getName() + " start" );
					loadingImg.setVisible(false);
					//loadingImg.setCaption(user.getName() + " finished deploying");
				    Notification notif = new Notification( deployerUser.getName(), Type.TRAY_NOTIFICATION);
				    notif.setDelayMsec(1000);
				    notif.setDescription("finished deployment");
					notif.show(ui.getPage());
				}
			}
		});
	}	
}
