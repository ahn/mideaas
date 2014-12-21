package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.mideaas.app.maven.BuildComponent;
import org.vaadin.mideaas.app.maven.Builder;
import org.vaadin.mideaas.app.maven.JettyComponent;
import org.vaadin.mideaas.app.maven.JettyUtil;
import org.vaadin.mideaas.app.maven.MavenCommand;
import org.vaadin.mideaas.app.test.TestCommand;
import org.vaadin.mideaas.ide.DefaultIdeConfiguration;
import org.vaadin.mideaas.ide.Ide;
import org.vaadin.mideaas.ide.IdeProject;
import org.vaadin.mideaas.ide.IdeUI;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;

public class MideaasIdeConfiguration extends DefaultIdeConfiguration {

	private final UserSettings userSettings;

	public MideaasIdeConfiguration(UserSettings userSettings) {
		this.userSettings = userSettings;
	}

	@Override
	public void ideCreated(Ide ide) {
		addSideBarComponents(ide);
		addMenuBarComponents(ide);
		ide.openDoc("index.html"); // ???
	}

	private void addSideBarComponents(Ide ide) {
		List<Component> components = new LinkedList<Component>();

		if (ide.getProject() instanceof VaadinProject) {
			VaadinProject vp = (VaadinProject) ide.getProject();
			if (vp.getBuilder() != null) {
				components.add(new BuildComponent(vp.getBuilder(), ide.getUser(), userSettings));
			}
			if (vp.getJettyServer() != null) {
				components.add(new JettyComponent(vp.getJettyServer()));
			}

			// Compiling all at the beginning
			vp.refreshClasspath();
			vp.compileAll();
		}
		else if (ide.getProject().getDoc("index.html") != null) {
			String url = ((IdeUI)IdeUI.getCurrent()).getServerRootUrl() + "raw/" + ide.getProject().getId() + "/index.html";
			Link link = new Link("View index.html", new ExternalResource(url));
			link.setTargetName("_blank");
			components.add(link);
		}

		ide.addSideBarComponents(components);
	}

	private void addMenuBarComponents(final Ide ide) {
		MenuBar menuBar = ide.getMenuBar();


		if (ide.getProject() instanceof VaadinProject) {
			VaadinProject vp = (VaadinProject) ide.getProject();
			addBuildMenu(menuBar, vp);
			addTestMenu(menuBar, vp);
			addDeployMenu(menuBar, vp, ide);
		}
	}

	@SuppressWarnings("serial")
	private void addDeployMenu(MenuBar menuBar, VaadinProject vp, final Ide ide) {
		MenuItem menu = menuBar.addItem("Deploy", null);
		menu.addItem("Deploy...", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				ide.setBelowEditorComponent(createBelowEditorComponent(ide), 100);
			}
		});
	}

	@SuppressWarnings("serial")
	private Component createBelowEditorComponent(final Ide ide) {
		VerticalLayout la = new VerticalLayout();
		la.addComponent(new Label("TODO: deploy"));
		Button b = new Button("Close");
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ide.setBelowEditorComponent(null);
			}
		});
		la.addComponent(b);
		return la;
	}


	private void addTestMenu(MenuBar menuBar, VaadinProject project) {
		MenuItem root = menuBar.addItem("Tests", null);
		root.addItem("Run tests...", new TestCommand(project));
	}

	@SuppressWarnings("serial")
	private void addBuildMenu(MenuBar menuBar, final VaadinProject project) {
		MenuItem menu = menuBar.addItem("Build", null);
		final Builder builder = project.getBuilder();
		menu.addItem("Compile widgetset", new MavenCommand(builder, new String[] {"vaadin:update-widgetset", "vaadin:compile"}));
		menu.addItem("Clean", new MavenCommand(builder, new String[] {"clean"}));

		menu.addItem("Stop all Jetty servers", new Command() {
			@Override
			public void menuSelected(MenuItem selected) {
				JettyUtil.stopAllJettys();
			}
		});

		menu.addItem("Update classpath", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				project.refreshClasspath();
			}
		});

		menu.addItem("Compile all", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				project.compileAll();
			}
		});
	}

	@Override
	public IdeProject createProject(String id, String name, Map<String, String> files, File workDir) {
		// TODO: more thorough project type checking
		if (files.containsKey("pom.xml")) {
			try {
				return new VaadinProject(id, name, workDir);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not create VaadinProject for the above reason");
			}
		}
		else {
			return new IdeProject(id, name, new WebProjectCustomizer(id));
		}
		return super.createProject(id, name, files, workDir);
	}

	//	@Override
	//	public IdeLoginView createLoginView() {
	//		String key = MideaasConfig.getProperty(Prop.GITHUB_KEY);
	//		String secret = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
	//		if (key == null || secret == null) {
	//			throw new IllegalArgumentException("No " + Prop.GITHUB_KEY + "/" + Prop.GITHUB_SECRET +" in config.");
	//		}
	//		return new GitHubLoginView(key, secret);
	//	}
	//
	//	@Override
	//	public IdeLobbyView createLobbyView() {
	//		String key = MideaasConfig.getProperty(Prop.GITHUB_KEY);
	//		String secret = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
	//		if (key == null || secret == null) {
	//			throw new IllegalArgumentException("No " + Prop.GITHUB_KEY + "/" + Prop.GITHUB_SECRET +" in config.");
	//		}
	//		return new GitHubLobbyView(key, secret);
	//	}


}
