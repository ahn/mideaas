package org.vaadin.oauth;

import org.vaadin.mideaas.model.User;


public class FBUser extends User {

	private final org.vaadin.oauth.OAuthButton.User fbuser;

	public FBUser(String id, org.vaadin.oauth.OAuthButton.User fbuser) {
		super(id,fbuser.getName());
		this.fbuser = fbuser;
	}

	public static FBUser newFBUser(org.vaadin.oauth.OAuthButton.User fbuser) {
		String id = newUserId();
		FBUser user = new FBUser(id,fbuser);
		return user;
	}
}
