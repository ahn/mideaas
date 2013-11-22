package org.vaadin.oauth;

import java.io.IOException;
import java.lang.reflect.Field;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.ui.Button;

/**
 * Starting point to create a {@link Button} that allows the user to log in
 * using OAuth; e.g log in with Facebook or Twitter.
 * <p>
 * Uses the Scribe oauth library, and it should be fairly straightforward to
 * implement a button for all supported services.
 * </p>
 * <p>
 * Generally, you just give the buttons the API keys that can be obtained from
 * the service in question, and a callback that will receive some user data once
 * the user has been authenticated. Some buttons implementations might provide
 * additional options (e.g get user email address from Facebook).
 * </p>
 * <p>
 * This approach is intentionally simplistic for this specific use-case: log in
 * with X. For more flexible OAuth interactions, the Scribe library can be used
 * directly.
 * </p>
 * 
 */
public abstract class OAuthButton extends Button {

    protected OAuthService service = null;
    protected Token requestToken = null;
    protected Token accessToken = null;

    protected String apiKey;
    protected String apiSecret;

    protected RequestHandler handler;

    protected OAuthListener authListener;
	private Page callbackPage;

    /**
     * @param caption
     *            button caption
     * @param apiKey
     *            API key from the service providing OAuth
     * @param apiSecret
     *            API secret from the service providing OAuth
     * @param authListener
     *            called once the user has been authenticated
     */
    public OAuthButton(String caption, String apiKey, String apiSecret,
            OAuthListener authListener) {
        super(caption);
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.authListener = authListener;
    }

    @Override
    protected void fireClick() {
        super.fireClick();
        authenticate();
    }

    protected void fireClick(MouseEventDetails details) {
        fireEvent(new Button.ClickEvent(this, details));
        authenticate();
    }

    /**
     * Gets the URL that the user will be sent to in order to authenticate. Most
     * implementations will also create the requestToken at this point.
     * 
     * @return authentication url for the OAuth service
     */
    protected abstract String getAuthUrl();

    /**
     * Gets the {@link Api} implementation class that this service uses.
     * 
     * @return {@link Api} implementation class
     */
    protected abstract Class<? extends Api> getApi();

    /**
     * Gets the name of the parameter that will contain the verifier when the
     * user returns from the OAuth service.
     * 
     * @return verifier parameter name
     */
    protected abstract String getVerifierName();

    private static final String[] oauthFails = new String[] { "oauth_problem" };

    /**
     * Gets the names of parameters that the OAuth service uses to indicate a
     * problem during authentication - e.g if the user presses 'Cancel' at the
     * authentication page.
     * 
     * @return
     */
    protected String[] getFailureParameters() {
        return oauthFails;
    }

    /**
     * Gets the URL from which JSON formatted user data can be fetched.
     * 
     * @return JSON user data url
     */
    protected abstract String getJsonDataUrl();

    /**
     * Gets the {@link User} implementation class for the user data that this
     * service provides.
     * 
     * @return {@link User} implementation class
     */
    protected abstract Class<? extends User> getUserClass();

    /**
     * Gets the OAuth service singleton.
     * 
     * @return OAuth service singleton
     */
    protected OAuthService getService() {
    	if (service == null) {
    		callbackPage = Page.getCurrent();
    		String location = callbackPage.getLocation().toString();
    		Class<? extends Api> api = getApi();
    		
    		ServiceBuilder builder = new ServiceBuilder();
            builder.provider(api);
            builder.apiKey(apiKey);
            builder.apiSecret(apiSecret);
            builder.callback(location);
            service=builder.build();
        }
        return service;
    }

    /**
     * Connects the parameter handler that will be invoked when the user comes
     * back, and sends the user to the authentication url for the OAuth service.
     */
    protected void authenticate() {
        if (handler == null) {
            handler = createRequestHandler();
            VaadinSession.getCurrent().addRequestHandler(handler);
        }

        //opens authentificationpage
        String url = getAuthUrl();
        this.callbackPage.open(url, "Authentificate", 400, 300, BorderStyle.DEFAULT);
    }

    /**
     * Creates the parameter handler that will be invoked when the user returns
     * from the OAuth service.
     * 
     * @return the parameter handler
     */
    protected RequestHandler createRequestHandler() {
        return new RequestHandler() {
			public boolean handleRequest(VaadinSession session,
					VaadinRequest request, VaadinResponse response)
					throws IOException {
                if (request.getParameterMap().containsKey(getVerifierName())) {
                    String v = request.getParameter(getVerifierName());
                    Verifier verifier = new Verifier(v);
                    accessToken = service
                            .getAccessToken(requestToken, verifier);

                    User user = getUser();

                    VaadinSession.getCurrent().removeRequestHandler(handler);
                    handler = null;
              
                    //String url = getAuthUrl();
                    //callbackPage.open(url, "Authentificate", 400, 300, BorderStyle.DEFAULT);
                    
                    authListener.userAuthenticated(user);

                } else if (getFailureParameters() != null) {
                    for (String key : getFailureParameters()) {
                        if (request.getParameterMap().containsKey(key)) {
                            authListener.failed(request.getParameter(key));
                            break;
                        }
                    }
                }
				return true;
            }
        };
    }

    /**
     * Creates and returns the {@link User} instance, usually by retreiving JSON
     * data from the url provided by {@link #getJsonDataUrl()}.
     * 
     * @return the {@link User} instance containing user data from the service
     */
    protected User getUser() {
        OAuthRequest request = new OAuthRequest(Verb.GET, getJsonDataUrl());
        service.signRequest(accessToken, request);
        Response response = request.send();

        Gson gson = new Gson();
        User user = gson.fromJson(response.getBody(), getUserClass());

        // TODO set the token/secret here?
        try {
            Field tokenField = user.getClass().getDeclaredField("token");
            if (tokenField != null) {
                tokenField.setAccessible(true);
                tokenField.set(user, accessToken.getToken());
            }

            Field tokenSecretField = user.getClass().getDeclaredField(
                    "tokenSecret");
            if (tokenSecretField != null) {
                tokenSecretField.setAccessible(true);
                tokenSecretField.set(user, accessToken.getSecret());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Called when the {@link User} instance has been successfully created, or
     * the OAuth service returned a problem code.
     */
    public interface OAuthListener {
        public void userAuthenticated(User user);

        public void failed(String reason);
    }

    /**
     * Contains user data common for most services. Some services might add own
     * data, or leave some data as null - for instance 'email' is quite seldom
     * available trough the APIs.
     * <p>
     * The default {@link OAuthButton#getUser()} implementation sets the 'token'
     * and 'tokenSecret' member fields if such exist, so that the {@link User}
     * implementation can just return these in {@link #getToken()} and
     * {@link #getTokenSecret()}.
     * </p>
     */
    public static interface User {

        /**
         * Name of the OAuth service, e.g "facebook".
         * 
         * @return
         */
        public String getService();

        /**
         * Often "Firstname Lastname", but not always - e.g Twitter users have a
         * single 'name' that can be changed to pretty much anything.
         * 
         * @return user name
         */
        public String getName();

        /**
         * The screen name is usually a short username used no the service, most
         * often unique, and quite often used to identify the user profile (e.g
         * http://twitter.com/screenname).
         * 
         * @return
         */
        public String getScreenName();

        /**
         * Url to the avatar picture for the user.
         * 
         * @return
         */
        public String getPictureUrl();

        /**
         * Id form the OAuth service; this is unique within the service. A
         * "globaly unique" id can be created for instance by combining this id
         * with the service name (e.g "facebook:12345").
         * 
         * @return
         */
        public String getId();

        /**
         * Url to the users public profile on the service (e.g
         * http://twitter.com/screenname).
         * 
         * @return
         */
        public String getPublicProfileUrl();

        /**
         * Email address - NOTE that this is quite seldom provided. Also, it
         * might be better to allow the user to provide an email address of
         * choice while registering for your service.
         * 
         * @return email address or (quite often) null
         */
        public String getEmail();

        /**
         * Gets the OAuth access token that can be used together with the token
         * secret ({@link #getTokenSecret()}) in order to access the OAuth
         * service API.
         * 
         * @return OAuth access token
         */
        public String getToken();

        /**
         * Gets the OAuth access token secret that can be used together with the
         * token ({@link #getToken()}) in order to access the OAuth service API.
         * 
         * @return OAuth access token secret
         */
        public String getTokenSecret();

    }
}