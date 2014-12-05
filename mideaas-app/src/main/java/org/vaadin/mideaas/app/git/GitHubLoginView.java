package org.vaadin.mideaas.app.git;

import org.vaadin.addon.oauthpopup.OAuthListener;
import org.vaadin.addon.oauthpopup.buttons.GitHubButton;
import org.vaadin.mideaas.ide.IdeLoginView;
import org.vaadin.mideaas.ide.IdeUI;
import org.vaadin.mideaas.ide.UserProfile;
import org.vaadin.mideaas.ide.UserToken;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class GitHubLoginView extends CustomComponent implements IdeLoginView, OAuthListener {

	private final String apiKey;
	private final String apiSecret;
	
	private final VerticalLayout layout = new VerticalLayout();
	private final Panel panel = new Panel();	

	public GitHubLoginView(String apiKey, String apiSecret) {
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		drawLayout();
		showLogin();
	}
	
	private void drawLayout() {
		getUI().getPage().setTitle("Welcome");
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		this.setSizeFull();
		
		panel.setCaption("Welcome");
		panel.setWidth("400px");
		panel.setHeight("400px");
		
		layout.setMargin(true);
		layout.setSpacing(true);
		panel.setContent(layout);
		
		la.addComponent(panel);
		la.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		
		setCompositionRoot(la);
	}
	
	private void showLogin() {
		
		layout.addComponent(new Label("Log in with your GitHub account to get started"));
		
		GitHubButton b = new GitHubButton(apiKey, apiSecret);
		b.setCaption("Log in");
		b.addOAuthListener(this);
		b.setScope("user:email,gist");
		layout.addComponent(b);
	}

	@Override
	public void authSuccessful(String accessToken, String accessTokenSecret) {
		UserToken token = new UserToken(accessToken, accessTokenSecret);
		loggedIn(token);
	}
	
	@Override
	public void authDenied(String reason) {
		// Do nothing?
	}
	
	private void loggedIn(UserToken token) {
		final GitHubService gh = new GitHubService(apiKey, apiSecret, token);
		final UserProfile profile = gh.getUserProfile();
		
		String name = profile.getName()!=null ? profile.getName() : profile.getIdentifier();
		GitHubIdeUser user = new GitHubIdeUser(profile.getIdentifier(), name, profile.getEmail());
		user.setGithubToken(token);
		
		((IdeUI)getUI()).logIn(user);
	}
	
	


	

}
