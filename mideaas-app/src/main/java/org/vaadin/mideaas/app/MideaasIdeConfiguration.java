package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.app.git.GitHubLobbyView;
import org.vaadin.mideaas.app.git.GitHubLoginView;
import org.vaadin.mideaas.app.java.VaadinProject;
import org.vaadin.mideaas.ide.DefaultIdeConfiguration;
import org.vaadin.mideaas.ide.ExampleProjectLobbyView;
import org.vaadin.mideaas.ide.IdeCustomizer;
import org.vaadin.mideaas.ide.IdeLobbyView;
import org.vaadin.mideaas.ide.IdeLoginView;
import org.vaadin.mideaas.ide.IdeProject;
import org.vaadin.mideaas.ide.JustUsernameLoginView;
import org.vaadin.mideaas.ide.ProjectCustomizer;
import org.vaadin.mideaas.ide.IdeUtil;

public class MideaasIdeConfiguration extends DefaultIdeConfiguration {


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

	@Override
	public IdeCustomizer getIdeCustomizer() {
		return new MideaasIdeCustomizer();
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
