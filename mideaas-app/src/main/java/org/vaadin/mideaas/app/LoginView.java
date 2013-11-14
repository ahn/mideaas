package org.vaadin.mideaas.app;

import org.vaadin.addon.oauthpopupbuttons.OAuthListener;
import org.vaadin.addon.oauthpopupbuttons.OAuthPopupButton;
import org.vaadin.addon.oauthpopupbuttons.buttons.FacebookButton;
import org.vaadin.addon.oauthpopupbuttons.buttons.TwitterButton;
import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.social.OAuthService;
import org.vaadin.mideaas.social.UserProfile;
import org.vaadin.mideaas.social.UserToken;
import org.vaadin.mideaas.social.OAuthService.Service;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class LoginView extends VerticalLayout implements View {
	
    private final MideaasUI ui;
	private final String nextView;

	private TextField loginField;
	private TextField emailField;

	
    public LoginView(MideaasUI ui, String nextView) {
    	this.ui = ui;
    	this.nextView = nextView;
        setMargin(true);
        initLogin();
    }
    
    @Override
	public void enter(ViewChangeEvent event) {
    	
	}

	/**
	 * Inits the loginscreen.
	 */
	private void initLogin() {
		System.out.println("initLogin");
		loginField = new TextField("Nick:");
		addComponent(loginField);
		
		emailField = new TextField("Email (optional):");
		addComponent(emailField);
		
		//button that fires the login action
		Button simpleLoginButton = new Button("Login");
		simpleLoginButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				String nick = (String) loginField.getValue();
				if (!nick.isEmpty()) {
					login(User.newUser(nick));
				}
			}
		});
		addComponent(simpleLoginButton);
		
		addComponent(new Label("&nbsp;", ContentMode.HTML));
		
		OAuthPopupButton fbButton = createFacebookButton();
		if (fbButton!=null) {
			addComponent(fbButton);
		}
		
		OAuthPopupButton twButton = createTwitterButton();
		if (twButton!=null) {
			addComponent(twButton);
		}
		
		OAuthPopupButton ghButton = createGitHubButton();
		if (ghButton!=null) {
			addComponent(ghButton);
		}
	}
	
	private OAuthPopupButton createFacebookButton() {
		String key = MideaasConfig.getProperty(Prop.FACEBOOK_KEY);
		String secret = MideaasConfig.getProperty(Prop.FACEBOOK_SECRET);
		if (key==null || secret==null) {
			return null;
		}
		FacebookButton button = new FacebookButton(key, secret);
		button.setCaption("Login with Facebook");
		return initButton(button, Service.FACEBOOK, key, secret);
	}
	
	private OAuthPopupButton createTwitterButton() {
		final String key = MideaasConfig.getProperty(Prop.TWITTER_KEY);
		final String secret = MideaasConfig.getProperty(Prop.TWITTER_SECRET);
		if (key==null || secret==null) {
			return null;
		}
		TwitterButton button = new TwitterButton(key, secret);
		button.setCaption("Login with Twitter");
		return initButton(button, Service.TWITTER, key, secret);
	}
	
	private OAuthPopupButton createGitHubButton() {
		String key = MideaasConfig.getProperty(Prop.GITHUB_KEY);
		String secret = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
		if (key==null || secret==null) {
			return null;
		}

		GitHubButton button = new GitHubButton(key, secret);
		button.setCaption("Login with GitHub");
		return initButton(button, Service.GITHUB, key, secret);
	}
	
	private OAuthPopupButton initButton(OAuthPopupButton button, final Service service, final String key, final String secret) {
		button.addListener(new OAuthListener() {
			@Override
			public void authSuccessful(String accessToken, String accessTokenSecret) {
				UserToken token = new UserToken(accessToken, accessTokenSecret);
				OAuthService serv = OAuthService.createService(service, key, secret, token);
				UserProfile profile = serv.getUserProfile();
				if (profile != null) {
					login(User.newUser(profile));
				}
				else {
					Notification.show("Not authenticated.");
				}
			}
			@Override
			public void authFailed(String reason) {
				Notification.show("Not authenticated.");
			}
		});
		return button;
	}
	
	

	private void login(User user) {
		ui.setUser(user);
		ui.navigateTo(nextView);
	}

	

}
