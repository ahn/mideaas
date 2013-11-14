package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.mideaas.frontend.MideaasEditor;
import org.vaadin.mideaas.frontend.MideaasEditor.CloseHandler;
import org.vaadin.mideaas.frontend.MideaasEditorPlugin;
import org.vaadin.mideaas.model.GitRepository;
import org.vaadin.mideaas.model.LobbyBroadcaster;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserSettings;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class EditorView extends CustomComponent implements View, CloseHandler {
	
	private final MideaasUI ui;
	private User user;
	
	public EditorView(String projectName, MideaasUI ui) {
		this.ui = ui;
		setSizeFull();
		Component editor = createEditor(projectName, ui);
		editor.setSizeFull();
		setCompositionRoot(editor);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		
	}
	
	private Component createEditor(String projectName, MideaasUI ui) {
		
		user = ui.getUser();
		if (user==null) {
			return new Label("Not logged in.");
		}
		
		SharedProject project = SharedProject.getProject(projectName);
		if (project==null) {
			return new Label("No such project: "+projectName);
		}
		
		project.addUser(user);
		LobbyBroadcaster.broadcastProjectsChanged();
		
		List<MideaasEditorPlugin> plugins = new LinkedList<MideaasEditorPlugin>();
		plugins.add(new ZipPlugin(project, user));
		try {
			plugins.add(new GitPlugin(project, user, GitRepository.fromExistingGitDir(project.getProjectDir())));
		} catch (IOException e) {
			System.err.println("WARNING: could not add git plugin!");
		}
		

		UserSettings settings = MideaasConfig.getDefaultUserSettings();
		plugins.add(new SettingsPlugin(settings));
		
		File fbf = MideaasConfig.getFeedbackFile();
		if (fbf != null) {
			plugins.add(new FeedbackPlugin(fbf));
		}
		
		plugins.add(new TestPlugin());
		
		MideaasEditor editor = new MideaasEditor(user, project, settings, plugins);
		editor.setTestingEnabled(!MideaasConfig.isExperiment());
		editor.setCloseHandler(this);
		
		return editor;
	}

	@Override
	public void closeRequested(SharedProject project) {
		project.removeUser(user);
		LobbyBroadcaster.broadcastProjectsChanged();
		ui.navigateTo("lobby");
	}

}
