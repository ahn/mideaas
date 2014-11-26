package org.vaadin.mideaas.ide.social;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TwitterService extends OAuthService {

	public TwitterService(String apiKey, String apiSecret, UserToken token) {
		super(Service.TWITTER, apiKey, apiSecret, token);
	}

	private Twitter twitter;
	
	@Override
	protected void connect() {
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(getApiKey(), getApiSecret());
		UserToken token = getUserToken();
		twitter.setOAuthAccessToken(new AccessToken(token.getToken(), token.getSecret()));
	}

	@Override
	protected UserProfile fetchUserProfile() {
	    try {
	    	String id = twitter.getScreenName();
	    	User u = twitter.showUser(id);
	    	String name = u.getName();
	    	String email = null;
	    	String imgUrl = u.getMiniProfileImageURL();
	    	return new UserProfile(Service.TWITTER, getUserToken(), id, name, email, imgUrl);
		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}
	}

}
