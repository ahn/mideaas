package org.vaadin.mideaas.app.git;

import org.vaadin.mideaas.ide.IdeUser;
import org.vaadin.mideaas.ide.oauth.UserToken;

public class GitHubIdeUser extends IdeUser {

	private UserToken githubToken;
	
	public GitHubIdeUser(String id, String name, String email) {
		super(id, name, email);
	}
	
	public void setGithubToken(String token, String secret) {
		setGithubToken(new UserToken(token, secret));
	}
	
	public void setGithubToken(UserToken token) {
		githubToken = token;
	}
	
	public UserToken getGithubToken() {
		return githubToken;
	}
}
