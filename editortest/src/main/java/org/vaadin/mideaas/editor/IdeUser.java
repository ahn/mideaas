package org.vaadin.mideaas.editor;

import org.vaadin.mideaas.editor.oauth.UserToken;

public class IdeUser {
	private final EditorUser editorUser;
	private UserToken githubToken;
	public IdeUser(String id, String name, String email) {
		editorUser = new EditorUser(id, name, email);
	}
	
	public String getId() {
		return editorUser.getId();
	}
	
	public String getName() {
		return editorUser.getName();
	}
	
	public String getEmail() {
		return editorUser.getEmail();
	}
	
	public EditorUser getEditorUser() {
		return editorUser;
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
