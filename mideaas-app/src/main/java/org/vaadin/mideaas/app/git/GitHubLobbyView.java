package org.vaadin.mideaas.app.git;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.github.GHRepository;
import org.vaadin.mideaas.app.git.GitProjectLoader.ProjectLoaderListener;
import org.vaadin.mideaas.ide.IdeLobbyView;
import org.vaadin.mideaas.ide.IdeUI;
import org.vaadin.mideaas.ide.UserProfile;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class GitHubLobbyView extends CustomComponent implements IdeLobbyView {

	private final String apiKey;
	private final String apiSecret;
	
	private final VerticalLayout layout = new VerticalLayout();
	private final Panel panel = new Panel();
	

	public GitHubLobbyView(String apiKey, String apiSecret) {
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}

	
	@Override
	public void enter(ViewChangeEvent event) {
		drawLayout();
		GitHubIdeUser user = getUser();
		if (user == null) {
			Notification.show("Error");
			return;
		}
		final GitHubService gh = new GitHubService(apiKey, apiSecret, user.getGithubToken());
		final UserProfile profile = gh.getUserProfile();
		showProjectOpener(gh, profile);
	}
	
	private void drawLayout() {
		getUI().getPage().setTitle("Welcome");
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		this.setSizeFull();
		
		panel.setCaption("Welcome");
		panel.setWidth("80%");
		panel.setHeight("80%");
		
		layout.setMargin(true);
		layout.setSpacing(true);
		panel.setContent(layout);
		
		la.addComponent(panel);
		la.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		
		setCompositionRoot(la);
	}
	
	private GitHubIdeUser getUser() {
		return (GitHubIdeUser) ((IdeUI)getUI()).getIdeUser();
	}
	
	

	private void showProjectOpener(final GitHubService gh, UserProfile profile) {
		panel.setCaption("Welcome, " + profile.getName() + "!");
		layout.removeAllComponents();
		layout.addComponent(createProjectOpener(gh, profile));
		layout.addComponent(createLogOutComponent());
	}
	
	private Component createProjectOpener(final GitHubService gh, final UserProfile profile) {
		
		VerticalLayout la = new VerticalLayout();
		la.setSpacing(true);

		la.addComponent(createGitHubCloneComponent(gh, profile));
		la.addComponent(createGitCloneComponent(gh, profile));
		
		return la;
	}
	
	private Component createGitHubCloneComponent(final GitHubService gh, final UserProfile profile) {
		Map<String, GHRepository> repos = gh.getMyPublicRepositories();
		
		final ListSelect reposSelect = new ListSelect();
		reposSelect.setRows(20);
		reposSelect.setWidth("100%");
		reposSelect.setNullSelectionAllowed(false);
		
		for (Entry<String, GHRepository> e : repos.entrySet()) {
			GHRepository repo = e.getValue();
			reposSelect.addItem(repo);
			reposSelect.setItemCaption(repo, repo.getName());
		}
		
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		la.setSpacing(true);
		
		la.addComponent(reposSelect);
		
		final Link link = new Link();
		link.setCaption("View at GitHub");
		link.setTargetName("_blank");
		link.setEnabled(false);
		
		final Button edit = new Button("Open");
		edit.setEnabled(false);
		
		reposSelect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				edit.setEnabled(true);
				GHRepository repo = (GHRepository) event.getProperty().getValue();
				link.setEnabled(true);
				link.setResource(new ExternalResource(repo.getUrl()));
			}
		});
		
		edit.setWidth("100%");
		edit.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					startGitHubProjet((GHRepository) reposSelect.getValue(), gh, profile);
				} catch (IOException e) {
					Notification.show("Error", Notification.Type.ERROR_MESSAGE);
				}
			}
		});
		
		HorizontalLayout ho = new HorizontalLayout();
		ho.setSpacing(true);
		ho.setWidth("100%");
		ho.addComponent(edit);
		ho.addComponent(link);
		ho.setExpandRatio(edit, 1);
		la.addComponent(ho);
		
		return new Panel("Select one of your (" + repos.size() + ") repositories to edit", la);
	}
	
	private Component createGitCloneComponent(final GitHubService gh, final UserProfile profile) {
		VerticalLayout ho = new VerticalLayout();
		ho.setSpacing(true);
		ho.setWidth("100%");
		ho.setMargin(true);

		final TextField nameField = new TextField("Name:");
		final TextField urlField = new TextField("Git URL:");
		urlField.setWidth("100%");
		Button b = new Button("Clone");
		b.setWidth("100%");
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String name = nameField.getValue();
				String url = urlField.getValue();
				if (name.isEmpty() || url.isEmpty()) {
					return;
				}
				startGitProject(name, url, "", "", new Runnable() {
					@Override
					public void run() {
						Notification.show("Error", Type.ERROR_MESSAGE);
						showProjectOpener(gh, profile);
					}
				});
			}
		});
		
		ho.addComponent(nameField);
		ho.addComponent(urlField);
		ho.addComponent(b);
		return new Panel("Or clone from URL", ho);
	}

	private Component createLogOutComponent() {
		Button b = new Button("Log out");
		b.setPrimaryStyleName(BaseTheme.BUTTON_LINK);
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				((IdeUI)getUI()).logOut();
			}
		});
		return b;
	}
	
	private void startGitHubProjet(final GHRepository repo, final GitHubService gh, final UserProfile profile) throws IOException {
		String url = repo.gitHttpTransportUrl();
		String name = repo.getName();

		// https://help.github.com/articles/git-automation-with-oauth-tokens/
		String username = gh.getUserToken().getToken();
		String password = "";
		
		startGitProject(name, url, username, password, new Runnable() {
			@Override
			public void run() {
				Notification.show("Error", Type.ERROR_MESSAGE);
				showProjectOpener(gh, profile);
			}
		});
	}
	
	private void startGitProject(final String name, String url, String username, String password, final Runnable onFailure) {
			GitProjectLoader loader = new GitProjectLoader();
		
		panel.setCaption("Loading project...");
		layout.removeAllComponents();
		layout.addComponent(loader);

		loader.initializeProjectFromGit(url, username, password, new ProjectLoaderListener() {
			@Override
			public void success(Map<String, String> contents) {
				((IdeUI) getUI()).startProject(name, contents);
			}
			
			@Override
			public void failure(String reason) {
				onFailure.run();
			}
		});
	}
	
}
