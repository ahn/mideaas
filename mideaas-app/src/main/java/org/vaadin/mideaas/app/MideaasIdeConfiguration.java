package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.app.git.GitHubLobbyView;
import org.vaadin.mideaas.app.git.GitHubLoginView;
import org.vaadin.mideaas.app.java.VaadinProject;
import org.vaadin.mideaas.app.maven.BuildComponent;
import org.vaadin.mideaas.app.maven.Builder;
import org.vaadin.mideaas.ide.DefaultIdeConfiguration;
import org.vaadin.mideaas.ide.Ide;
import org.vaadin.mideaas.ide.IdeLobbyView;
import org.vaadin.mideaas.ide.IdeLoginView;
import org.vaadin.mideaas.ide.IdeProject;
import org.vaadin.mideaas.ide.IdeUtil;
import org.vaadin.mideaas.ide.ProjectCustomizer;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class MideaasIdeConfiguration extends DefaultIdeConfiguration {

	private final UserSettings userSettings;
	
	public MideaasIdeConfiguration(UserSettings userSettings) {
		this.userSettings = userSettings;
	}
	
	@Override
	public void ideCreated(Ide ide) {
		addSideBarComponents(ide);
		addMenuBarComponents(ide);
	}
	
	private void addSideBarComponents(Ide ide) {
		List<Component> components = new LinkedList<Component>();
		Builder builder = new Builder((VaadinProject) ide.getProject(), userSettings);
		components.add(new BuildComponent(builder, ide.getUser()));
		ide.addSideBarComponents(components);
	}
	
	private void addMenuBarComponents(final Ide ide) {
		MenuBar menuBar = ide.getMenuBar();
		MenuItem menu = menuBar.addItem("Moi", null);
		menu.addItem("Show some component", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				ide.setBelowEditorComponent(createBelowEditorComponent("comp1", ide), 100);
			}
		});
		menu.addItem("Show another component", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				ide.setBelowEditorComponent(createBelowEditorComponent("comp2", ide), 100);
			}
		});
	}

	private Component createBelowEditorComponent(String text, final Ide ide) {
		Button b = new Button("Close " + text);
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ide.setBelowEditorComponent(null);
			}
		});
		return b;
	}

	@Override
	public IdeProject createProject(String id, String name, Map<String, String> files) {
		if (files.containsKey("pom.xml")) { // TODO: more detailed check
			try {
				return new VaadinProject(id, name, createProjectDir(files));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not create VaadinProject for the above reason");
			}
		}
		return super.createProject(id, name, files);
	}

	@Override
	public ProjectCustomizer getProjectCustomizer(IdeProject project) {
		if (project instanceof VaadinProject) {
			// ???
		}
		return new MideaasProjectCustomizer();
	}

	private static File createProjectDir(Map<String, String> files) throws IOException {
		Path path = Files.createTempDirectory("mideaas-vaadin");
		IdeUtil.saveFilesToPath(files, path);
		return path.toFile();
	}
	
	@Override
	public IdeLoginView createLoginView() {
		String key = MideaasConfig.getProperty(Prop.GITHUB_KEY);
		String secret = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
		if (key == null || secret == null) {
			throw new IllegalArgumentException("No " + Prop.GITHUB_KEY + "/" + Prop.GITHUB_SECRET +" in config.");
		}
		return new GitHubLoginView(key, secret);
	}

	@Override
	public IdeLobbyView createLobbyView() {
		String key = MideaasConfig.getProperty(Prop.GITHUB_KEY);
		String secret = MideaasConfig.getProperty(Prop.GITHUB_SECRET);
		if (key == null || secret == null) {
			throw new IllegalArgumentException("No " + Prop.GITHUB_KEY + "/" + Prop.GITHUB_SECRET +" in config.");
		}
		return new GitHubLobbyView(key, secret);
	}

	
}
