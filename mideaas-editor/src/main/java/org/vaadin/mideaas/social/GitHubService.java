package org.vaadin.mideaas.social;


import java.io.IOException;

import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;

public class GitHubService extends OAuthService {

	public GitHubService(String apiKey, String apiSecret, UserToken token) {
		super(Service.GITHUB, apiKey, apiSecret, token);
	}

	private GitHub gh;
	
	@Override
	protected void connect() {
		try {
			gh = GitHub.connectUsingOAuth(getUserToken().getToken());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected UserProfile fetchUserProfile() {
		if (gh==null) {
			return null;
		}
		
		try {
			GHMyself me = gh.getMyself();
			String name = me.getName();
			String email = me.getEmail();
			String imgUrl = me.getAvatarUrl();
			return new UserProfile(Service.GITHUB, getUserToken(), name, email, imgUrl);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	   
	}

}
