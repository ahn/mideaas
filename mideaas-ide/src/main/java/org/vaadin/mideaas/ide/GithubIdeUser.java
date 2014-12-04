package org.vaadin.mideaas.ide;

import org.vaadin.mideaas.ide.oauth.UserToken;

public class GithubIdeUser extends IdeUser {

	private UserToken githubToken;
	
	public GithubIdeUser(String id, String name, String email) {
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
