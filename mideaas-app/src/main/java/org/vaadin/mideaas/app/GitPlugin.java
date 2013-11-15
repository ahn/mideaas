package org.vaadin.mideaas.app;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.addon.oauthpopupbuttons.OAuthListener;
import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.frontend.MideaasEditorPlugin;
import org.vaadin.mideaas.model.GitRepository;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.social.GitHubService;
import org.vaadin.mideaas.social.UserProfile;
import org.vaadin.mideaas.social.UserToken;
import org.vaadin.mideaas.social.OAuthService.Service;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class GitPlugin implements MideaasEditorPlugin {
	
	private static final String GITHUB_KEY = MideaasConfig.getProperty(Prop.GITHUB_KEY);
	private static final String GITHUB_SECRET = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
	
	public class PushWindow extends Window {
		
		private VerticalLayout layout = new VerticalLayout();
		
		public PushWindow() {
			super("Push");
		}
		
		@Override
		public void attach() {
			super.attach();
			layout.setMargin(true);
			setContent(layout);
			
			UserProfile profile = user.getProfile(Service.GITHUB);
			if (profile!=null) {
				pushable(profile.getToken());
			}
			else {
				notPushable();
			}
			
		}

		private void notPushable() {
			layout.removeAllComponents();
			
			GitHubButton ghb = new GitHubButton(GITHUB_KEY, GITHUB_SECRET);
			ghb.setScope("repo");
			ghb.setPopupWindowFeatures("resizable");
			ghb.setCaption("Authorize with GitHub");
			ghb.addListener(new OAuthListener() {
				@Override
				public void authSuccessful(String accessToken, String accessTokenSecret) {
					GitHubService service = new GitHubService(GITHUB_KEY, GITHUB_SECRET, new UserToken(accessToken, accessTokenSecret));
					UserProfile profile = service.getUserProfile();
					user.addProfile(profile);
					pushable(user.getProfile(Service.GITHUB).getToken());
				}
				
				@Override
				public void authFailed(String reason) {
					Notification.show("Not authorized.");
				}
			});
			layout.addComponent(ghb);
		}

		private void pushable(final UserToken token) {
			layout.removeAllComponents();
			
			layout.addComponent(new Label("Authorized with GitHub"));
			
			Button b = new Button("Push to GitHub");
			b.addClickListener(new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					try {
						gitPush(token.getToken());
					} catch (GitAPIException e) {
						Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			});
			layout.addComponent(b);
		}
	}

	private final SharedProject project;
	private final User user;
	private final GitRepository repo;
	
	public GitPlugin(SharedProject project, User user, GitRepository repo) {
		this.project = project;
		this.user = user;
		this.repo = repo;
	}

	@Override
	public void extendMenu(MenuBar menuBar) {

		Command commit = createCommitCommand();
		Command push = createPushCommand();
		
		MenuItem gitItem = menuBar.addItem("Git", null);

		if (commit != null) {
			gitItem.addItem("Commit", commit);
		}

		if (push != null) {
			gitItem.addItem("Push", push);
		}
	}

	
	private Command createPushCommand() {
		if (GITHUB_KEY==null || GITHUB_SECRET==null) {
			return null;
		}
		
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				PushWindow w = new PushWindow();
				UI.getCurrent().addWindow(w);
			}
		};
	}


	private Command createCommitCommand() {
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window window = new Window();
				window.setCaption("Commit");
				window.center();
				window.setWidth(200, Unit.PIXELS);
				VerticalLayout layout = new VerticalLayout();
				final TextField field = new TextField("Commit Message:");
				Button button = new Button("Commit");
				layout.addComponent(field);
				layout.addComponent(button);

				window.setContent(layout);
				UI.getCurrent().addWindow(window);

				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						try {
							commitLocal(project.getName(), (String) field.getValue());
							window.close();
						} catch (GitAPIException e) {
							Notification.show(e.getMessage());
						}
					}
				});
			}
		};
	}
	
	protected void commitLocal(String projectName, String msg)
			throws GitAPIException {
		gitCommitAll(msg);
	}

	public void gitCommitAll(String msg) throws GitAPIException {
		repo.addSourceFilesToGit();
		repo.commitAll(msg);
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
