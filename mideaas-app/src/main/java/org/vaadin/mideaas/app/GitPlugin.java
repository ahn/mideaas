package org.vaadin.mideaas.app;

import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.app.model.GitRepository;
import org.vaadin.mideaas.ide.MideaasEditorPlugin;
import org.vaadin.mideaas.ide.model.SharedProject;
import org.vaadin.mideaas.ide.model.User;

import com.vaadin.server.ClassResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class GitPlugin implements MideaasEditorPlugin {
	
	public static final String GITHUB_KEY = MideaasConfig.getProperty(Prop.GITHUB_KEY);
	public static final String GITHUB_SECRET = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
	
	private final SharedProject project;
	private final User user;
	private final GitRepository repo;
	private MenuItem gitHubItem;
	
	public GitPlugin(SharedProject project, User user, GitRepository repo) {
		this.project = project;
		this.user = user;
		this.repo = repo;
	}

	@Override
	public void extendMenu(MenuBar menuBar, SharedProject project) {

		Command commit = createCommitCommand();
		Command github = createGitHubCommand();
		
		MenuItem gitItem = menuBar.addItem("Git", null);

		if (commit != null) {
			gitItem.addItem("Commit", commit);
		}
		
		if (github != null) {
			gitHubItem = gitItem.addItem("GitHub", github);
			gitHubItem.setIcon(new ClassResource("/org/vaadin/addon/oauthpopupbuttons/icons/github16.png"));
			gitHubItem.setEnabled(repo.hasCommit());
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
				w.center();
				UI.getCurrent().addWindow(w);
			}
		};
	}


	private Command createCommitCommand() {
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				Window w = new GitCommitWindow(GitPlugin.this, repo, project.getName());
				w.center();
				UI.getCurrent().addWindow(w);
			}
		};
	}

	public MenuItem getGitHubItem() {
		return gitHubItem;
	}
	


}
