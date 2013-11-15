package org.vaadin.mideaas.app;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.addon.oauthpopupbuttons.OAuthListener;
import org.vaadin.addon.oauthpopupbuttons.buttons.GitHubButton;
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
	
	public static final String GITHUB_KEY = MideaasConfig.getProperty(Prop.GITHUB_KEY);
	public static final String GITHUB_SECRET = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
	
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
		Command github = createGitHubCommand();
		
		MenuItem gitItem = menuBar.addItem("Git", null);

		if (commit != null) {
			gitItem.addItem("Commit", commit);
		}

		if (github != null) {
			gitItem.addItem("GitHub", github);
		}
	}

	
	private Command createGitHubCommand() {
		if (GITHUB_KEY==null || GITHUB_SECRET==null) {
			return null;
		}
		
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				GitHubWindow w = new GitHubWindow(user, repo, project.getName());
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
							commitAll(project.getName(), (String) field.getValue());
							window.close();
						} catch (GitAPIException e) {
							Notification.show(e.getMessage());
						}
					}
				});
			}
		};
	}
	
	protected void commitAll(String projectName, String msg)
			throws GitAPIException {
		gitCommitAll(msg);
	}

	public void gitCommitAll(String msg) throws GitAPIException {
		repo.addSourceFilesToGit();
		repo.commitAll(msg);
	}

}
