package org.vaadin.mideaas.ide;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHRepository;
import org.vaadin.addon.oauthpopup.OAuthListener;
import org.vaadin.addon.oauthpopup.buttons.GitHubButton;
import org.vaadin.mideaas.ide.oauth.GitHubService;
import org.vaadin.mideaas.ide.oauth.UserProfile;
import org.vaadin.mideaas.ide.oauth.UserToken;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class WelcomeView extends CustomComponent implements View, OAuthListener {

	private static final String API_KEY = "97a7e251c538106e7922";
	private static final String API_SECRET = "6a36b0992e5e2b00a38c44c21a6e0dc8ae01d83b";
	
	private final VerticalLayout layout = new VerticalLayout();
	private final Panel panel = new Panel();
	private final IdeProject project;
	private final String filename;
	

	public WelcomeView() {
		this(null, null);
	}
	
	public WelcomeView(IdeProject project) {
		this(project, null);
	}

	public WelcomeView(IdeProject project, String filename) {
		this.project = project;
		this.filename = filename;
		
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		this.setSizeFull();
		
		panel.setCaption("Welcome");
		panel.setWidth("400px");
		panel.setHeight("400px");
		
		layout.setMargin(true);
		layout.setSpacing(true);
		panel.setContent(layout);
		
		la.addComponent(panel);
		la.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		
		setCompositionRoot(la);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		getUI().getPage().setTitle("Welcome");
		IdeUser user = ((IdeUI)getUI()).getIdeUser();
		if (user != null && user.getGithubToken() != null) {
			loggedIn(user.getGithubToken());
		}
		else {
			showLogin();
		}
	}
	
	private void showLogin() {
		panel.setCaption("Welcome");
		
		layout.removeAllComponents();
		layout.addComponent(new Label("Log in with your GitHub account to edit your gists"));
		
		GitHubButton b = new GitHubButton(API_KEY, API_SECRET);
		b.setCaption("Log in");
		b.addOAuthListener(this);
		b.setScope("user:email,gist");
		layout.addComponent(b);
		
		if (project==null) {
			layout.addComponent(new Label("or"));
			
			Button demo = new Button("try a demo project");
			demo.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					IdeUI ui = (IdeUI) getUI();
					ui.setSessionUser(new IdeUser(UUID.randomUUID().toString(), "Demo user", null));
					ProjectCustomizer cust = ((IdeUI)getUI()).getProjectCustomizer();
					ui.startProject(Util.createDemoProject(cust));
				}
			});
			
			layout.addComponent(demo);
		}
	}

	@Override
	public void authSuccessful(String accessToken, String accessTokenSecret) {
		getSession().setAttribute("github-access-token", accessToken);
		getSession().setAttribute("github-access-token-secret", accessTokenSecret);
		UserToken token = new UserToken(accessToken, accessTokenSecret);
		loggedIn(token);
	}
	
	@Override
	public void authDenied(String reason) {
		// Do nothing?
	}
	
	private void loggedIn(UserToken token) {
		final GitHubService gh = new GitHubService(API_KEY, API_SECRET, token);
		final UserProfile profile = gh.getUserProfile();
		
		String name = profile.getName()!=null ? profile.getName() : profile.getIdentifier();
		IdeUser user = new IdeUser(profile.getIdentifier(), name, profile.getEmail());
		user.setGithubToken(token);
		((IdeUI) getUI()).setSessionUser(user);

		if (project==null) {
			showProjectOpener(gh, name, profile);
			//showGistsOpener(gh, name, profile);
		}
		else {
			// TODO: this probably should be somewhere else...
			getUI().getPage().setTitle(project.getName());
			IdeCustomizer cust = ((IdeUI)getUI()).getIdeCustomizer();
			Ide ide = new Ide(project, user, cust);
			ide.setSizeFull();
			setSizeFull();
			setCompositionRoot(ide);
			if (filename != null) {
				ide.openDoc(filename);
			}
		}
	}
	
	private void showProjectOpener(final GitHubService gh, String name, UserProfile profile) {
		panel.setCaption("Welcome, " + name + "!");
		layout.removeAllComponents();
		layout.addComponent(createProjectOpener(gh, profile));
		layout.addComponent(createLogOutComponent());
	}
	
	private Component createProjectOpener(final GitHubService gh, UserProfile profile) {
		Map<String, GHRepository> repos = gh.getMyPublicRepositories();
		
		
		final ListSelect reposSelect = new ListSelect("Select one of your (" + repos.size() + ") repositories to edit ");
		reposSelect.setRows(9);
		reposSelect.setWidth("100%");
		reposSelect.setNullSelectionAllowed(false);
		
		for (Entry<String, GHRepository> e : repos.entrySet()) {
			System.out.println("repo: " + e.getKey());
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
					startProjectForRepo((GHRepository) reposSelect.getValue(), gh);
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

	

	private void showGistsOpener(final GitHubService gh, String name, UserProfile profile) {
		panel.setCaption("Welcome, " + name + "!");
		layout.removeAllComponents();
		layout.addComponent(createOwnGistOpener(gh, profile));
		layout.addComponent(createPublicGistOpener(gh));
		layout.addComponent(createLogOutComponent());
	}
	
	private Component createLogOutComponent() {
		Button b = new Button("Log out");
		b.setPrimaryStyleName(BaseTheme.BUTTON_LINK);
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				((IdeUI)getUI()).removeSessionUser();
				showLogin();
			}
		});
		return b;
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
	
	
	private void startProjectForRepo(GHRepository repo, GitHubService gh) throws IOException {
		for (Entry<String, GHBranch> e : repo.getBranches().entrySet()) {
			if ("master".equals(e.getKey())) {
				Map<String, String> contents = getRepoContents(repo, ".");
				ProjectCustomizer cust = ((IdeUI)getUI()).getProjectCustomizer();
				IdeProject project = Util.createProject(contents, cust);
				((IdeUI) getUI()).startProject(project);
				return;
			}
		}
	}
	
	

	private Map<String, String> getRepoContents(GHRepository repo, String path) {
		Map<String, String> files = new HashMap<String, String>();
		List<GHContent> cont;
		try {
			cont = repo.getDirectoryContent(path);
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
		for (GHContent c : cont) {
			System.out.println(c.getName() + " -- " + c.getPath());
			if (c.isDirectory()) {
				files.putAll(getRepoContents(repo, c.getPath()));
			}
			else {
				try {
					files.put(c.getPath(), c.getContent());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return files;
	}

	

}