package org.vaadin.mideaas.app;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.mideaas.model.GitRepository;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class GitCommitWindow extends Window {
	
	private GitPlugin plugin;
	private GitRepository repo;
	private String projectName;
	
	private VerticalLayout layout = new VerticalLayout();
	

	public GitCommitWindow(GitPlugin plugin, GitRepository repo, String projectName) {
		super("Git Commit");
		this.plugin = plugin;
		this.repo = repo;
		this.projectName = projectName;
		
		setWidth(200, Unit.PIXELS);
		setContent(layout);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		final TextField field = new TextField("Commit Message:");
		Button button = new Button("Commit");
		layout.addComponent(field);
		layout.addComponent(button);
		
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					commitAll(projectName, (String) field.getValue());
					close();
				} catch (GitAPIException e) {
					Notification.show(e.getMessage());
				}
			}
		});
	}
	
	protected void commitAll(String projectName, String msg)
			throws GitAPIException {
		gitCommitAll(msg);
	}

	public void gitCommitAll(String msg) throws GitAPIException {
		repo.addSourceFilesToGit();
		repo.commitAll(msg);
		plugin.getGitHubItem().setEnabled(repo.hasCommit());
	}

}
