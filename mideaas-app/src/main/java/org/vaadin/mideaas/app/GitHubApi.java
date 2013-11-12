package org.vaadin.mideaas.app;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;

public class GitHubApi extends DefaultApi20 {

	private static final String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
	private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
	
	@Override
	public String getAccessTokenEndpoint() {
		return ACCESS_TOKEN_URL;
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
		String scope = config.getScope()==null ? "" : config.getScope();
		return AUTHORIZE_URL+"?client_id="+config.getApiKey()+"&scope="+scope;
	}

}
