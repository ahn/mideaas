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
		Map<String, GHRepository> repos = gh.getMyPublicRepositories();
		
		final ListSelect reposSelect = new ListSelect("Select one of your (" + repos.size() + ") repositories to edit ");
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
					startProjectForRepo((GHRepository) reposSelect.getValue(), gh, profile);
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
		
		return la;
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
	
	private void startProjectForRepo(final GHRepository repo, final GitHubService gh, final UserProfile profile) throws IOException {
		String url = repo.gitHttpTransportUrl();
		
		GitProjectLoader loader = new GitProjectLoader();
		
		panel.setCaption("Loading project...");
		layout.removeAllComponents();
		layout.addComponent(loader);

		// https://help.github.com/articles/git-automation-with-oauth-tokens/
		String username = gh.getUserToken().getToken();
		String password = "";
		loader.initializeProjectFromGit(url, username, password, new ProjectLoaderListener() {
			@Override
			public void success(Map<String, String> contents) {
				((IdeUI) getUI()).startProject(repo.getName(), contents);
			}
			
			@Override
			public void failure(String reason) {
				Notification.show("Error", Type.ERROR_MESSAGE);
				showProjectOpener(gh, profile);
			}
		});
	}	

	
/*
	private void showGistsOpener(final GitHubService gh, String name, UserProfile profile) {
		panel.setCaption("Welcome, " + name + "!");
		layout.removeAllComponents();
		layout.addComponent(createOwnGistOpener(gh, profile));
		layout.addComponent(createPublicGistOpener(gh));
		layout.addComponent(createLogOutComponent());
	}
	


	private Component createOwnGistOpener(final GitHubService gh, UserProfile profile) {
		
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		
		List<GHGist> gists = gh.getGists(profile.getIdentifier());
		
		final ListSelect gistSelect = new ListSelect("Select one of your (" + gists.size() + ") gists to edit ");
		gistSelect.setRows(9);
		gistSelect.setWidth("100%");
		gistSelect.setNullSelectionAllowed(false);
		
		for (final GHGist gist : gists) {
			gistSelect.addItem(gist);
			String d = gist.getDescription();
			String caption;
			if (d.isEmpty()) {
				caption = "gist:"+gist.getId()+"";
			} else {
				caption = d.length() > 20 ? d.substring(0, 18) + "..." : d;
			}
			gistSelect.setItemCaption(gist, caption);
		}
		
		
		la.addComponent(gistSelect);
		
		final Link link = new Link();
		link.setCaption("View at GitHub");
		link.setTargetName("_blank");
		link.setEnabled(false);
		
		final Button edit = new Button("Open");
		edit.setEnabled(false);
		
		gistSelect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				edit.setEnabled(true);
				GHGist gist = (GHGist) event.getProperty().getValue();
				link.setEnabled(true);
				link.setResource(new ExternalResource(gist.getHtmlUrl()));
			}
		});
		
		edit.setWidth("100%");
		edit.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				startProjectForGist(((GHGist) gistSelect.getValue()).getId(), gh);
			}
		});
		
		HorizontalLayout ho = new HorizontalLayout();
		ho.setSpacing(true);
		ho.setWidth("100%");
		ho.addComponent(edit);
		ho.addComponent(link);
		ho.setExpandRatio(edit, 1);
		
		la.addComponent(ho);
		
		return la;
	}

	private Component createPublicGistOpener(final GitHubService gh) {
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();

		la.addComponent(new Label("or open a public Gist"));
		
		HorizontalLayout ho = new HorizontalLayout();
		ho.setSizeFull();
		la.addComponent(ho);
		
		
		final TextField tf = new TextField(null, "5c5b916a1cc468927904");
		tf.setWidth("100%");
		ho.addComponent(tf);
		
		Button b = new Button("Edit");
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				startProjectForGist(tf.getValue(), gh);
			}
		});
		
		ho.addComponent(b);
		ho.setExpandRatio(tf, 1);
		
		return la;
	}
	
	private void startProjectForGist(String gistId, GitHubService gh) {
		GHGist realGist = gh.getGist(gistId);
		ProjectCustomizer cust = ((IdeUI)getUI()).getProjectCustomizer();
		IdeProject project = Util.projectFromGist(realGist, cust);
		((IdeUI) getUI()).startProject(project);
	}
	*/
	
}
