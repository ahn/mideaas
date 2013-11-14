package org.vaadin.mideaas.social;

import org.vaadin.mideaas.social.OAuthService.Service;


public class UserProfile {
	
	private final Service service;
	private final UserToken token;
	private final String name;
	private final String email;
	private final String imgUrl;
	
	public UserProfile(Service service, UserToken token, String name, String email, String imgUrl) {
		this.service = service;
		this.token = token;
		this.name = name;
		this.email = email;
		this.imgUrl = imgUrl;
	}

	public Service getService() {
		return service;
	}

	public UserToken getToken() {
		return token;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getImgUrl() {
		return imgUrl;
	}
}
