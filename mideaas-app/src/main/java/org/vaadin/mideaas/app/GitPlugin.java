package org.vaadin.mideaas.app;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.mideaas.frontend.MideaasEditorPlugin;
import org.vaadin.mideaas.model.GitRepository;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class GitPlugin implements MideaasEditorPlugin {
	
	private final static String PASSWORD_STRING = "Use userName, passWord";
	private final static String OAUTH_STRING = "Use OAuth";

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

		Command commitLocal = createCommitLocalCommand();
		Command commitRemote = createCommitRemoteCommand();
		Command changeOrigin = createChangeOriginCommand();
		
		MenuItem gitItem = menuBar.addItem("Git", null);

		if (commitLocal != null) {
			gitItem.addItem("Commit", commitLocal);
		}

		if (commitRemote != null) {
			gitItem.addItem("Push", commitRemote);
		}

		if (changeOrigin != null) {
			gitItem.addItem("Set Origin", changeOrigin);
		}
	}
	
	/**
	 * Show origin if project has been initialized from remote git repository.
	 *
	 * @return the string
	 */
	public String showOrigin(){
		return repo.showOrigin();
	}
	
	public void setOrigin(String value) {
		repo.setOrigin(value);
	}

	private Command createChangeOriginCommand() {
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				// create window
				final Window window = new Window();
				window.setWidth(600, Unit.PIXELS);
				window.setCaption("Set origin");

				// create layout
				final VerticalLayout layout = new VerticalLayout();

				final TextField field = new TextField("Url:");
				String origin = repo.showOrigin();
				field.setValue(origin==null?"":origin);
				Button button = new Button("Set origin");

				layout.addComponent(field);
				layout.addComponent(button);

				ClickListener clicklistener = new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						String value = (String) field.getValue();
						setOrigin(value);
						window.close();
					}
				};
				button.addClickListener(clicklistener);

				window.setContent(layout);
				UI.getCurrent().addWindow(window);
			}
		};
	}

	/**
	 * Creates the commit local runnable.
	 * 
	 * @param projectName
	 *            the project name
	 * @return the runnable
	 */
	private Command createCommitLocalCommand() {
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

	/**
	 * Commit local.
	 * 
	 * @param projectName
	 *            the project name
	 * @param msg
	 *            the msg
	 * @throws GitAPIException
	 *             the git api exception
	 */
	protected void commitLocal(String projectName, String msg)
			throws GitAPIException {
		gitCommit(msg);
	}
	
	public void gitCommit(String msg) throws GitAPIException {
		repo.addSourceFilesToGit();
		repo.commitAll(msg);
	}
	

	/**
	 * Creates the commit remote runnable.
	 * 
	 * @param projectName
	 *            the project name
	 * @return the runnable
	 */
	private Command createCommitRemoteCommand() {
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {

				String originUrl = repo.showOrigin();
				if (originUrl == null || originUrl.isEmpty()) {
					Notification.show("You have to set origin first!");
					return;
				}

//				String token = user.getOAuthToken();
//				if (token != null) {
//					try {
//						gitPush(token);
//						return;
//					} catch (GitAPIException e) {
//						// did not work, then we ask for authentification or
//						// token
//					}
//				}

				// create window
				final Window window = new Window();
				window.setWidth(600, Unit.PIXELS);
				window.setCaption("Push to remote repository");

				// create layouts (first is for optiongroup and second is for
				// changing content)
				final VerticalLayout layout = new VerticalLayout();
				final VerticalLayout layout2 = new VerticalLayout();

				// textFields
				final TextField field1 = new TextField("");
				final PasswordField field2 = new PasswordField("");

				// Create a optiongroup
				OptionGroup selector = new OptionGroup("Login to gitrepository");
				selector.setInvalidAllowed(false);
				selector.setImmediate(true);
				selector.setNullSelectionAllowed(false);
				selector.addItem(PASSWORD_STRING);
				selector.addItem(OAUTH_STRING);
				ValueChangeListener listener = new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						final String value = (String) event.getProperty()
								.getValue();
						populateCommitRemoteWindow(layout2, value, field1,
								field2, user);
					}
				};
				selector.select(OAUTH_STRING);
				selector.addValueChangeListener(listener);
				layout.addComponent(selector);
				populateCommitRemoteWindow(layout2, OAUTH_STRING, field1, field2, user);

				layout.addComponent(layout2);

				Button button = new Button("Push");
				layout.addComponent(button);
				ClickListener clicklistener = new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						try {
							String value1 = (String) field1.getValue();
							String value2 = (String) field2.getValue();
							if (value2.equals("")) {
								gitPush(value1);
								user.setOAuthToken(value1);
							} else {
								gitPush(value1, value2);
							}
							window.close();
						} catch (GitAPIException e) {
							// TODO Auto-generated catch block
							Notification.show(e.getMessage());
						}
					}
				};
				button.addClickListener(clicklistener);

				window.setContent(layout);
				UI.getCurrent().addWindow(window);
			}
		};
	}

	static private void populateCommitRemoteWindow(Layout layout, String value,
			final TextField field1, final PasswordField field2, User user) {
		layout.removeAllComponents();
		if (value.equals(PASSWORD_STRING)) {
			Label text = new Label(
					"We promise not to use your password to any evil things, but you should consider changing it anyway soon after using this feature :)");
			field1.setCaption("Username for gitrepository");
			field1.setValue("");
			field2.setCaption("Password");
			layout.addComponent(text);
			layout.addComponent(field1);
			layout.addComponent(field2);
		} else {
			Label text = new Label(
					"<h3>You need to generate OAuth token.</h3>"
							+ "Read more from: <a href='https://help.github.com/articles/creating-an-oauth-token-for-command-line-use'>tutorial</a><br/> "
							+ "or  use: <b>curl --insecure -u 'yourUserIDHere' -d '{\"scopes\":[\"repo\"],\"note\":\"Help example\"}' https://api.github.com/authorizations </b><br/>");
			text.setContentMode(ContentMode.HTML);
			field1.setCaption("OAuth token");
			layout.addComponent(text);
			layout.addComponent(field1);
			field1.setValue(user.getOAuthToken()==null?"":user.getOAuthToken());
		}
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
