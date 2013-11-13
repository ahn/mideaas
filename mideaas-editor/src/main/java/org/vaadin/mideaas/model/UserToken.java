package org.vaadin.mideaas.model;

public class UserToken {
	public enum Service {
		GITHUB,
		FACEBOOK,
		TWITTER,
		ETC
	}

	private final Service service;
	private final String token;
	private final String secret;
	
	public UserToken(Service service, String token, String secret) {
		this.service = service;
		this.token = token;
		this.secret = secret;
	}

	public Service getService() {
		return service;
	}

	public String getToken() {
		return token;
	}

	public String getSecret() {
		return secret;
	}
	
	

}
