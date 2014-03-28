package org.vaadin.mideaas.frontend;

import java.io.File;
import java.io.IOException;

import org.vaadin.mideaas.frontend.Deployer.DeployListener;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserSettings;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.jasoft.qrcode.QRCode;

@SuppressWarnings("serial")
public class DeployComponent extends CustomComponent implements DeployListener{
	
	private final Deployer deployer;
	private final SharedProject project;
	private final UserSettings settings;
	
	private final LogView logView = new LogView();
	private CFAppsView view;

	private UI ui;

	private VerticalLayout layout = new VerticalLayout();
	private VerticalLayout resultLayout = new VerticalLayout();

	private QRCode qrCode = new QRCode();
	private Link link = new Link("Link To App", null);

	final private Button deployButton = new Button("Deploy app");
	final private Button cancelButton = new Button("Cancel");
	final private Button stopButton = new Button("Delete all apps");
	final private Button showButton = new Button("Show all apps");
	final private Button showLogButton = new ShowLogButton("XMLMessage Log", logView);

	private Embedded loadingImg = new Embedded(null, new ThemeResource(
			"../base/common/img/loading-indicator.gif"));
	protected String responseMessage = null;
	private Button showResponseDataButton;

	public DeployComponent(SharedProject project, UserSettings settings, User user) {
		super();
		this.project = project;
		this.settings=settings;
		String pathToWar = new File(project.getTargetPathFor(user).getAbsolutePath(), project.getName()+".war").getAbsolutePath();
		this.deployer = new Deployer(pathToWar, settings.coapsApiUri);
	}
	
	private void buildLayout() {
		Panel p = new Panel("CloudFoundry panel");
		p.setContent(layout);
		
		//TODO: add stuff here
		if (settings.useSlaSelectionMap){
			createSLANavigationFeatures();
		}
		
		layout.addComponent(deployButton);
		
		//layout for showing deployment results
		layout.addComponent(resultLayout);
		loadingImg.setVisible(false);
		layout.addComponent(loadingImg);
		cancelButton.setVisible(false);
		layout.addComponent(cancelButton);
		layout.addComponent(showButton);
		layout.addComponent(stopButton);
		layout.addComponent(showLogButton);		
		
		//adds qr code
		qrCode.setVisible(false);
		qrCode.setWidth("128px");
		qrCode.setHeight("128px");
		layout.addComponent(qrCode);

		//link to the deployed application
		link.setVisible(false);
		layout.setMargin(true);
		setCompositionRoot(p);
		layout.addComponent(link);
		
		//buttonhandlers
		this.deployButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				deploy();
			}	
		});

		this.cancelButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				networkingCancelled("Deploy cancelled");
			}	
		});

		
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
	}

	private void createSLANavigationFeatures() {
		Button slabutton = new Button("Negotiate SLA");			
		slabutton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				VaadinSession.getCurrent().addRequestHandler(
						new RequestHandler() {
						    @Override
						    public boolean handleRequest(VaadinSession session,
						                                 VaadinRequest request,
						                                 VaadinResponse response)
						            throws IOException {
								VaadinSession.getCurrent().removeRequestHandler(this);
								final UI ui = getUI();
								final String url = request.getPathInfo();
								// TODO Auto-generated method stub
								ui.access(new Runnable(){
									@Override
									public void run() {
										setMessage("ok, uri was: " + url);
									}
								}
								);
					            return true;
						    }
						}
				);
				String params = "/addParametersAndVallbackURLHere";
				getUI().getPage().open(settings.slaSelectionMapUri+params, "newWindow");
			}
		});
		layout.addComponent(slabutton);
		showResponseDataButton = new Button("ShowResponseData");
		showResponseDataButton.setEnabled(false);
		layout.addComponent(showResponseDataButton);
		//buttonhandlers
		this.showResponseDataButton.addClickListener(new Button.ClickListener() {				
			@Override
			public void buttonClick(ClickEvent event) {
				String message = getMessage();
				Notification.show(message);
			}
		});
	}

	private String getMessage() {
		// TODO Auto-generated method stub
		return this.responseMessage;
	}	
	
	private void setMessage(String message) {
		this.responseMessage=message;
		if (message==null){
			showResponseDataButton.setEnabled(false);
		}else{
			showResponseDataButton.setEnabled(true);
		}
	}	
	
	@Override
	public void attach() {
		super.attach();
		buildLayout();
		setUi(UI.getCurrent());
		deployer.addDeployListener(this);
	}

	@Override
	public void detach() {
		super.detach();
		deployer.removeDeployListener(this);
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
		deployer.deploy(this.settings, this.project, this.logView, this.link, this.qrCode);
		this.updateCFAppsView();
	}

	private void finishDeploy() {
		loadingImg.setVisible(false);
		deployButton.setEnabled(true);
		deployButton.setCaption("Deploy");
		cancelButton.setVisible(false);
	}

	private void deployFail(String s) {
		Label lab = new Label(s);
		lab.setIcon(Icons.CROSS_CIRCLE);
		resultLayout.addComponent(lab);
	}

	private void deploySuccess() {
		Label lab = new Label("Deploy successful");
		lab.setIcon(Icons.TICK_CIRCLE);
		resultLayout.addComponent(lab);
	}

	@Override
	public void networkingFinished(final boolean success, final String msg) {
		getUi().access(new Runnable() {
			@Override
			public void run() {
				finishDeploy();
				if (success) {
					deploySuccess();
				} else {
					deployFail(msg);
					qrCode.setVisible(false);
					link.setVisible(false);
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
				link.setVisible(false);
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
				cancelButton.setVisible(true);
				loadingImg.setVisible(true);
				loadingImg.setCaption(msg);
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
			link.setVisible(false);
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
}
