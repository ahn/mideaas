package org.vaadin.mideaas.app;

import org.vaadin.mideaas.model.User;
import org.vaadin.oauth.FBUser;
import org.vaadin.oauth.FacebookButton;
import org.vaadin.oauth.OAuthButton.OAuthListener;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class LoginPanel extends Panel implements OAuthListener {

    private MideaasUI ui;

    private VerticalLayout mainLayout = new VerticalLayout();
	private TextField simpleLoginField;
	private TextField emailField;

	private User user;
	
    public LoginPanel(MideaasUI ui, User user) {
        this.ui = ui;
        this.user = user;
        this.setContent(mainLayout);
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();
		mainLayout.removeAllComponents();
    	initSimpleLogin();
    }

	/**
	 * Inits the loginscreen.
	 */
	private void initSimpleLogin() {
		simpleLoginField = new TextField("Nick:");
		mainLayout.addComponent(simpleLoginField);
		
		emailField = new TextField("Email (optional):");
		mainLayout.addComponent(emailField);
		
		//button that fires the login action
		Button simpleLoginButton = new Button("Login");
		simpleLoginButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				String nick = (String) simpleLoginField.getValue();
				if (!nick.isEmpty()) {
					User user= User.newUser(nick);
					setLoggedInUser(user);
					ui.loggedIn(user);
				}
			}
		});
		mainLayout.addComponent(simpleLoginButton);
		
		mainLayout.addComponent(new Label("&nbsp;", ContentMode.HTML));

		FacebookButton button = new FacebookButton("141905209336309", "9770d931e88104028e07cda983b33ab5", this);
		mainLayout.addComponent(button);
	}


	private void drawLoggedIn() {
		mainLayout.removeAllComponents();
		mainLayout.addComponent(new Label("Logged in as " + user.getName()));
		Button logout = new Button("Log out");
		logout.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				setLoggedInUser(null);
			}
		});
		mainLayout.addComponent(logout);
	}
	
	public interface LoggedInUserListener {
		void loggedInUserChanged(User user);
	}

	public void setLoggedInUser(User user) {
		if (user == null && this.user != null) {
			this.user = null;
			attach();
		} else if (user != null
				&& (this.user == null || this.user.getUserId() != user
						.getUserId())) {
			this.user = user;
			drawLoggedIn();
		}
	}

	@Override
	public void userAuthenticated(org.vaadin.oauth.OAuthButton.User fbuser) {
		FBUser user= FBUser.newFBUser(fbuser);
		if (user!=null){
			setLoggedInUser(user);
			ui.loggedIn(user);
		}else{
			failed("User already logged in...");
		}
	}

	@Override
	public void failed(String reason) {
		ui.logout();
		if(Page.getCurrent()!=null) { // ???
			Notification.show(reason);
		}
	}
}
