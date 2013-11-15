package org.vaadin.mideaas.app;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.vaadin.addon.oauthpopupbuttons.OAuthListener;
import org.vaadin.addon.oauthpopupbuttons.buttons.GitHubButton;
import org.vaadin.mideaas.model.GitRepository;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.social.GitHubService;
import org.vaadin.mideaas.social.UserProfile;
import org.vaadin.mideaas.social.UserToken;
import org.vaadin.mideaas.social.OAuthService.Service;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


@SuppressWarnings("serial")
public class GitHubWindow extends Window {
	
		
	private VerticalLayout layout = new VerticalLayout();
		

	private final User user;
	private final GitRepository repo;
	private final String projectName;
	
	public GitHubWindow(User user, GitRepository repo, String projectName) {
		super("GitHub");
		this.user = user;
		this.repo = repo;
		this.projectName = projectName;
		layout.setMargin(true);
		setContent(layout);
	}
	
	@Override
	public void attach() {
		super.attach();
		redraw();
	}
	
	private void redraw() {
		layout.removeAllComponents();
		
		UserProfile profile = user.getProfile(Service.GITHUB);
		if (profile==null) {
			drawGitHubButton();
			return;
		}
		else {
			redrawConnected(profile);
		}
		
		
		
	}
	
	private void redrawConnected(UserProfile profile) {
		String origin = repo.getOrigin();
		if (origin==null) {
			drawNewProject(profile);
		}
		else if (isGitHubOrigin(origin)) {
			drawPush(profile);
		}
		else {
			layout.addComponent(new Label("Git repository origin is not from GitHub."));
		}
	}

	private void drawGitHubButton() {
		final String key = GitPlugin.GITHUB_KEY;
		final String secret = GitPlugin.GITHUB_SECRET;
		GitHubButton ghb = new GitHubButton(key, secret);
		ghb.setScope("repo");
		ghb.setPopupWindowFeatures("resizable");
		ghb.setCaption("Authorize with GitHub");
		ghb.addListener(new OAuthListener() {
			@Override
			public void authSuccessful(String accessToken, String accessTokenSecret) {
				GitHubService service = new GitHubService(key, secret, new UserToken(accessToken, accessTokenSecret));
				UserProfile profile = service.getUserProfile();
				user.addProfile(profile);
				redraw();
			}
			
			@Override
			public void authFailed(String reason) {
				Notification.show("Not authorized.");
			}
		});
		layout.addComponent(ghb);
	}

	private static boolean isGitHubOrigin(String origin) {
		return origin.contains("github.com"); // TODO better
	}
	
	private void drawNewProject(final UserProfile profile) {
		Button bu = new Button("Create project in GitHub");
		layout.addComponent(bu);
		bu.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				newProject(profile);
			}
		});
	}
	
	protected void newProject(UserProfile profile) {
		final String key = GitPlugin.GITHUB_KEY;
		final String secret = GitPlugin.GITHUB_SECRET;
		UserToken t = profile.getToken();
		GitHubService service = new GitHubService(key, secret, new UserToken(t.getToken(), t.getSecret()));
		try {
			String origin = service.createRepository(projectName);
			repo.setOrigin(origin);
			Notification.show("Project created");
			redraw();
			
		} catch (IOException e) {
			Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

	private void drawPush(final UserProfile profile) {
		layout.addComponent(new Label("Authorized with GitHub"));
		
		Button b = new Button("Push to GitHub");
		b.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					gitPush(profile.getToken().getToken());
				} catch (GitAPIException e) {
					Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		});
		layout.addComponent(b);
	}
	
	
	/**
	 * Push to Gitrepository using OAuth token.
	 * 
	 * Token can be acquired for example by writing:
	 *  	curl --insecure -u 'janne.lautamaki@tut.fi' -d '{"scopes":["repo"],"note":"Help example"}' https://api.github.com/authorizations
	 * Then password is asked:
	 *		Enter host password for user 'janne.lautamaki@tut.fi':
	 * And finally we get response with the token that can be used
	 *		{
	 *		  "id": 2670499,
	 *		  "url": "https://api.github.com/authorizations/2670499",
	 *		  "app": {
	 *		    "name": "Help example (API)",
	 *		    "url": "http://developer.github.com/v3/oauth/#oauth-authorizations-api",
	 *		    "client_id": "9923f92ec0bd6f800a48"
	 *		  },
	 *		  "token": "f7b76c1d74b34079cbe1ea3f5156cc4b62060b88",
	 *		  "note": "Help example",
	 *		  "note_url": null,
	 *		  "created_at": "2013-05-29T10:23:56Z",
	 *		  "updated_at": "2013-05-29T10:23:56Z",
	 *		  "scopes": [
 	 *			"repo"
	 *		  ]
	 *		}
	 *
	 * @param oauthToken the oauth token
	 * @throws GitAPIException the git api exception
	 */
	private void gitPush(String oauthToken) throws GitAPIException {
		gitPush(oauthToken,"");
	}
	
	/**
	 * Push to Gitrepository using userName and password (maybe not so secure :) ).
	 *
	 * @param userName the user name
	 * @param passWord the pass word
	 * @throws GitAPIException the git api exception
	 */
	private void gitPush(String userName, String passWord)
			throws GitAPIException {
		repo.pushAll(userName, passWord);
	}
}
