package org.vaadin.mideaas.app.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.Project;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.hooks.CommitMsgHook;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.addon.oauthpopup.OAuthListener;
import org.vaadin.addon.oauthpopup.buttons.GitHubButton;
import org.vaadin.mideaas.app.Icons;
import org.vaadin.mideaas.app.MideaasConfig;
import org.vaadin.mideaas.app.MideaasUI;
import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.ide.IdeProjectWithWorkDir;
import org.vaadin.mideaas.ide.UserToken;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

public class GitHubMenu {

	public static void addMenu(MenuBar menuBar, IdeProjectWithWorkDir project,
			GitHubIdeUser user) {

		try {
			doAddMenu(menuBar, project, user);
		} catch (IOException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("serial")
	private static class StatusCommand implements Command {
		
		private final GitRepository repo;
		private IdeProjectWithWorkDir project;

		public StatusCommand(IdeProjectWithWorkDir project, GitRepository repo) {
			this.project = project;
			this.repo = repo;
		}

		@Override
		public void menuSelected(MenuItem selectedItem) {
			
			project.writeToDisk();
			
			Status status;
			String branch;
			try {
				branch = repo.getBranch();
				status = repo.status();
			} catch (GitAPIException | IOException e) {
				e.printStackTrace();
				return; // ???
			}

			Window w = new Window();
			w.center();
			w.setWidth("80%");
			w.setHeight("80%");
			
			UI.getCurrent().addWindow(w);
			
			VerticalLayout la = new VerticalLayout();
			la.setSizeFull();
			
			la.addComponent(new Label("On branch " + branch));
			
			final TwinColSelect sel = new TwinColSelect();
			sel.setSizeFull();
			sel.setLeftColumnCaption("Workspace");
			sel.setRightColumnCaption("Staging Area (to be committed)");
			la.addComponent(sel);
			la.setExpandRatio(sel, 1);
			
			for (String mf : status.getModified()) {
				sel.addItem(mf);
			}

			for (String mf : status.getUntracked()) {
				sel.addItem(mf);
				sel.setItemIcon(mf, Icons.PLUS);
			}

			final Button commitButton = new Button("Commit");
			commitButton.setEnabled(false);
			la.addComponent(commitButton);
			
			final TextField commitMsg = new TextField("Commit message");
			la.addComponent(commitMsg);
			
			ValueChangeListener vcl = new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					commitButton.setEnabled(!commitMsg.getValue().isEmpty() && !((Collection<Object>) sel.getValue()).isEmpty());
				}
			};
			
			sel.addValueChangeListener(vcl);
			commitMsg.addValueChangeListener(vcl);
			
			
			commitButton.addClickListener(new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					Set<String> files = new HashSet<String>();
					
					for (Object obj : (Collection<Object>) sel.getValue()) {
						files.add((String) obj);
					}
					try {
						repo.commit(files, commitMsg.getValue());
					} catch (GitAPIException e) {
						e.printStackTrace(); // ???
					}
				}
			});
			
			w.setContent(la);
		}

	};
	
	@SuppressWarnings("serial")
	private static class DiffCommand implements Command {
		
		private final GitRepository repo;
		private IdeProjectWithWorkDir project;

		public DiffCommand(IdeProjectWithWorkDir project, GitRepository repo) {
			this.project = project;
			this.repo = repo;
		}

		@Override
		public void menuSelected(MenuItem selectedItem) {
			
			project.writeToDisk();
			
			Status status;
			String branch;
			try {
				branch = repo.getBranch();
				status = repo.status();
			} catch (GitAPIException | IOException e) {
				e.printStackTrace();
				return; // ???
			}

			Window w = new Window();
			w.center();
			w.setWidth("80%");
			w.setHeight("80%");
			
			UI.getCurrent().addWindow(w);
			
			VerticalLayout la = new VerticalLayout();
			la.setSizeFull();
			
			la.addComponent(new Label("On branch " + branch));
			
			VerticalSplitPanel split = new VerticalSplitPanel();
			split.setSizeFull();
			la.addComponent(split);
			la.setExpandRatio(split, 1);
			
			Set<String> modifiedFiles = status.getModified();
			Set<String> untrackedFiles = status.getUntracked();
			
			final ListSelect sel = new ListSelect(modifiedFiles.size()+" modified, " + untrackedFiles + " untracked files");
			sel.setSizeFull();
			sel.setNullSelectionAllowed(false);
			split.setFirstComponent(sel);
			
			for (String mf : modifiedFiles) {
				sel.addItem(mf);
			}

			for (String mf : untrackedFiles) {
				sel.addItem(mf);
				sel.setItemIcon(mf, Icons.PLUS);
			}
			
			final AceEditor ace = new AceEditor();
			ace.setMode(AceMode.diff);
			ace.setShowGutter(false);
			ace.setReadOnly(true);
			ace.setSizeFull();
			split.setSecondComponent(ace);
			
			
			sel.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					String selected = (String) sel.getValue();
					try {
						String diff = repo.diffToHead(selected);
						ace.setReadOnly(false);
						ace.setValue(diff);
						ace.setReadOnly(true);
					} catch (GitAPIException | IOException e) {
						e.printStackTrace(); // ???
					}
				}
			});
			
			w.setContent(la);
		}

	};
	
	@SuppressWarnings("serial")
	private static class LogCommand implements Command {
		
		private final GitRepository repo;
		private IdeProjectWithWorkDir project;

		public LogCommand(IdeProjectWithWorkDir project, GitRepository repo) {
			this.project = project;
			this.repo = repo;
		}

		@Override
		public void menuSelected(MenuItem selectedItem) {
						
			String branch;
			try {
				branch = repo.getBranch();
			} catch (IOException e) {
				e.printStackTrace();
				return; // ???
			}

			Window w = new Window();
			w.center();
			w.setWidth("80%");
			w.setHeight("80%");
			
			UI.getCurrent().addWindow(w);
			
			VerticalLayout la = new VerticalLayout();
			la.setSizeFull();
			
			Iterable<RevCommit> log;
			try {
				log = repo.log();
			} catch (GitAPIException e1) {
				e1.printStackTrace();
				return; //
			}
			
			la.addComponent(new Label("On branch " + branch));
			
			Table sel = new Table();
			sel.addContainerProperty("Commit", String.class, null);
			sel.addContainerProperty("Author", String.class, null);
			sel.addContainerProperty("Message", String.class, null);
			sel.setSizeFull();
			la.addComponent(sel);
			la.setExpandRatio(sel, 1);
			
			for (RevCommit li : log) {
				String author = li.getAuthorIdent().getName() + "<" + li.getAuthorIdent().getEmailAddress() + ">";
				sel.addItem(new Object[]{ li.getId().getName(), author, li.getShortMessage() }, li.getId().getName());
			}
			
			w.setContent(la);
		}

	};
	
	@SuppressWarnings("serial")
	private static class RemotesCommand implements Command {
		
		private final GitRepository repo;
		private IdeProjectWithWorkDir project;

		public RemotesCommand(IdeProjectWithWorkDir project, GitRepository repo) {
			this.project = project;
			this.repo = repo;
		}

		@Override
		public void menuSelected(MenuItem selectedItem) {
			
						
			String branch;
			try {
				branch = repo.getBranch();
			} catch (IOException e) {
				e.printStackTrace();
				return; // ???
			}

			Window w = new Window();
			w.center();
			w.setWidth("80%");
			w.setHeight("80%");
			
			UI.getCurrent().addWindow(w);
			
			VerticalLayout la = new VerticalLayout();
			la.setSpacing(true);
			//la.setSizeFull();		
			la.addComponent(new Label("On branch " + branch));
			
			final ComboBox remoteSel = new ComboBox("Remote:");
			remoteSel.setNullSelectionAllowed(false);
			la.addComponent(remoteSel);
			
			for (String remote : repo.getRemotes()) {
				remoteSel.addItem(remote);
			}
			remoteSel.select("origin");
			
			final Button pullButton = new Button("Pull");
			pullButton.setEnabled(remoteSel.getValue() != null);
			pullButton.setCaption("Pull from " + remoteSel.getValue());
			la.addComponent(pullButton);
			
			final Button pushButton = new Button("Push");
			pushButton.setEnabled(remoteSel.getValue() != null);
			pushButton.setCaption("Push to " + remoteSel.getValue());
			la.addComponent(pushButton);
			
			remoteSel.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					pullButton.setCaption("Pull from " + remoteSel.getValue());
					pullButton.setEnabled(remoteSel.getValue() != null);

					pushButton.setCaption("Push to " + remoteSel.getValue());
					pushButton.setEnabled(remoteSel.getValue() != null);
				}
			});
			pullButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					try {
						PullResult result = repo.pullFrom((String) remoteSel.getValue());
						if (result.isSuccessful()) {
							Notification.show("Successfully pulled");
						}
						else {
							Notification.show("Pull failed!", Notification.Type.ERROR_MESSAGE);
						}
					} catch (GitAPIException e) {
						e.printStackTrace();
						Notification.show("Pull failed: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
					}
				}
			});
			
			pushButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					try {
						MideaasUI ui = (MideaasUI) UI.getCurrent();
						GitHubIdeUser user = (GitHubIdeUser) ui.getIdeUser();
						// https://github.com/blog/1270-easier-builds-and-deployments-using-git-over-https-and-oauth
						repo.pushTo((String) remoteSel.getValue(), user.getGithubToken().getToken(), "x-oauth-basic");
						Notification.show("Pushed");
					} catch (GitAPIException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			String apiKey = MideaasConfig.getProperty(Prop.GITHUB_KEY);
			String apiSecret = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
			GitHubButton b = new GitHubButton(apiKey, apiSecret);
			b.setCaption("Authorize access to GitHub repositories");
			b.setScope("user:email,repo");
			b.addOAuthListener(new OAuthListener() {
				@Override
				public void authSuccessful(String accessToken, String accessTokenSecret) {
					UserToken token = new UserToken(accessToken, accessTokenSecret);
					GitHubIdeUser user = (GitHubIdeUser) ((MideaasUI)UI.getCurrent()).getIdeUser();
					user.setGithubToken(token);
				}
				
				@Override
				public void authDenied(String reason) {
					// ?
				}
			});
			la.addComponent(b);
			
			w.setContent(la);
		}
		

	};

	private static void doAddMenu(MenuBar menuBar,
			IdeProjectWithWorkDir project, GitHubIdeUser user) throws IOException, GitAPIException {
		File dir = project.getWorkDir().toFile();
		System.out.println("GITDIR: " + dir);
		GitRepository repo = GitRepository.fromExistingGitDir(dir);


		MenuItem root = menuBar.addItem("Git", null);
		root.addItem("Commit", new StatusCommand(project, repo));
		root.addItem("Diff", new DiffCommand(project, repo));
		root.addItem("Log", new LogCommand(project, repo));
		root.addItem("Remotes", new RemotesCommand(project, repo));
	}
}
