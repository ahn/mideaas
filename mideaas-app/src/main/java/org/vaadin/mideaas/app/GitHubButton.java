package org.vaadin.mideaas.app;

import org.vaadin.addon.oauthpopupbuttons.OAuthPopupButton;

import com.vaadin.server.ClassResource;

@SuppressWarnings("serial")
public class GitHubButton extends OAuthPopupButton {
	
	public GitHubButton(String key, String secret) {
		super(GitHubApi.class, key, secret);
		
		setIcon(new ClassResource("/org/vaadin/mideaas/app/icons/github16.png"));
		setCaption("GitHub");
	}

}
