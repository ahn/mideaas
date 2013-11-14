package org.vaadin.mideaas.app;

import java.io.IOException;

import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.vaadin.addon.oauthpopupbuttons.OAuthListener;
import org.vaadin.addon.oauthpopupbuttons.buttons.FacebookButton;
import org.vaadin.addon.oauthpopupbuttons.buttons.TwitterButton;
import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserToken.Service;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
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
		
		FacebookButton fbButton = createFacebookButton();
		if (fbButton!=null) {
			addComponent(fbButton);
		}
		
		TwitterButton twButton = createTwitterButton();
		if (twButton!=null) {
			addComponent(twButton);
		}
		
		GitHubButton ghButton = createGitHubButton();
		if (ghButton!=null) {
			addComponent(ghButton);
		}
	}


	private FacebookButton createFacebookButton() {
		String key = MideaasConfig.getProperty(Prop.FACEBOOK_KEY);
		String secret = MideaasConfig.getProperty(Prop.FACEBOOK_SECRET);
		if (key==null || secret==null) {
			return null;
		}

		FacebookButton button = new FacebookButton(key, secret);
		button.setCaption("Login with Facebook");
		button.addListener(new OAuthListener() {
			@Override
			public void authSuccessful(String accessToken, String accessTokenSecret) {
				FacebookClient client = new DefaultFacebookClient(accessToken);
				com.restfb.types.User me = client.fetchObject("me", com.restfb.types.User.class);
				User user = User.newUser(me.getName());
				user.setToken(Service.FACEBOOK, accessToken, accessTokenSecret);
				login(user);
			}
			
			@Override
			public void authFailed(String reason) {
				Notification.show("Not authenticated.");
			}
		});
		return button;
	}
	
	private TwitterButton createTwitterButton() {
		final String key = MideaasConfig.getProperty(Prop.TWITTER_KEY);
		final String secret = MideaasConfig.getProperty(Prop.TWITTER_SECRET);
		if (key==null || secret==null) {
			return null;
		}

		TwitterButton button = new TwitterButton(key, secret);
		button.setCaption("Login with Twitter");
		button.addListener(new OAuthListener() {
			@Override
			public void authSuccessful(String accessToken, String accessTokenSecret) {
				Twitter twitter = new TwitterFactory().getInstance();
				twitter.setOAuthConsumer(key, secret);
				twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));
			    try {
			    	User user = User.newUser(twitter.getScreenName());
			    	user.setToken(Service.TWITTER, accessToken, accessTokenSecret);
					login(user);
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			@Override
			public void authFailed(String reason) {
				Notification.show("Not authenticated.");
			}
		});
		return button;
	}
	
	private GitHubButton createGitHubButton() {
		String key = MideaasConfig.getProperty(Prop.GITHUB_KEY);
		String secret = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
		if (key==null || secret==null) {
			return null;
		}

		GitHubButton button = new GitHubButton(key, secret);
		button.setCaption("Login with GitHub");
		button.addListener(new OAuthListener() {
			@Override
			public void authSuccessful(String accessToken, String accessTokenSecret) {
				try {
					GitHub client = GitHub.connectUsingOAuth(accessToken);
					GHMyself me = client.getMyself();
					User user = User.newUser(me.getName());
					user.setToken(Service.GITHUB, accessToken, accessTokenSecret);
					login(user);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
