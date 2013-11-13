package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.frontend.ClaraEditor;
import org.vaadin.mideaas.frontend.JettyUtil;
import org.vaadin.mideaas.frontend.MavenUtil;
import org.vaadin.mideaas.frontend.MideaasEditor;
import org.vaadin.mideaas.frontend.MideaasEditorPlugin;
import org.vaadin.mideaas.model.GitRepository;
import org.vaadin.mideaas.model.LobbyBroadcaster;
import org.vaadin.mideaas.model.ProjectLog;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserSettings;
import org.vaadin.mideaas.model.ZipUtils;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


@PreserveOnRefresh
@SuppressWarnings("serial")
@Theme("reindeer")
@Push
public class MideaasUI extends UI implements MideaasEditor.CloseHandler {

	private User user;

	private final VerticalLayout mainLayout = new VerticalLayout();

	static {
		applyConfig();
	}

	private static void applyConfig() {
		
		ProjectLog.setLogDir(MideaasConfig.getLogDir());
		
		JettyUtil.setPortRange(
				MideaasConfig.getPropertyInt(Prop.JETTY_PORT_MIN),
				MideaasConfig.getPropertyInt(Prop.JETTY_PORT_MAX));

		JettyUtil.setStopPortRange(
				MideaasConfig.getPropertyInt(Prop.JETTY_STOP_PORT_MIN),
				MideaasConfig.getPropertyInt(Prop.JETTY_STOP_PORT_MAX));
		
		
		JettyUtil.stopAllJettys(); // XXX TODO???

		MavenUtil.setMavenHome(new File(MideaasConfig
				.getProperty(Prop.MAVEN_HOME)));

		ClaraEditor.setVisualDesignerUrl(MideaasConfig
				.getProperty(Prop.VISUAL_DESIGNER_URL));
		
		try {
			SharedProject.initializeProjectRoot(
					new File(MideaasConfig.getProperty(Prop.PROJECTS_DIR)),
					MideaasConfig.getProperty(Prop.APP_PACKAGE_BASE));
		} catch (IOException e) {
			System.err.println("ERROR: could not initialize project dir: " + e.getMessage());
			System.exit(-1);
		}
	}

	@Override
	protected void init(VaadinRequest request) {
		mainLayout.setSizeFull();
		setContent(mainLayout);
		drawLobby();
	}

	@Override
	public void detach() {
		logout();
		super.detach();
	}

	private void drawLobby() {
		mainLayout.removeAllComponents();
		Component content;
		if (this.user == null) {
			content = createLoginPanel();
		} else {
			content = new LobbyPanel(this, user);
		}
		mainLayout.addComponent(content);
		mainLayout.setExpandRatio(content, 1);
		mainLayout
				.addComponent(new Label(
						"Some icons by <a href=\"http://p.yusukekamiyamane.com/\">Yusuke Kamiyamane</a>.",
						ContentMode.HTML));
	}

	private Component createLoginPanel() {
		if (MideaasConfig.isExperiment()) {
			return new ExperimentLoginPanel(this);
		}
		else {
			return new LoginPanel(this);
		}
	}

	/**
	 * Creates new project and returns false if fails.
	 * 
	 */
	public boolean newProject(String projectName, boolean createSkeleton) {
		if (SharedProject.getProjectNames().contains(projectName)) {
			Notification.show(projectName + " already exists",
					Notification.Type.HUMANIZED_MESSAGE);
			return false;
		} else {
			SharedProject project = SharedProject.createNewProject(projectName);
			if (project == null) {
				return false;
			}
			// TODO: where should we do the git repository stuff... ??
			try {
				GitRepository repo = GitRepository.initAt(project.getProjectDir());
				repo.addSourceFilesToGit();
			} catch (IOException | GitAPIException e) {
				System.err.println("WARNING: could not initialize Git repository at " + project.getProjectDir());
			}

			broadcastNewProject(projectName);
			return true;
		}
	}

	/**
	 * Clones project from git.
	 * 
	 * @param gitUrl
	 *            the git url
	 */
	public void createGitProject(String gitUrl) {
		String projectName = GitRepository.defaultProjectNameFromGitUrl(gitUrl);
		SharedProject project = SharedProject.createEmptyProject(projectName);
		if (project != null) {
			GitRepository.cloneFrom(gitUrl, project.getProjectDir());
			project.refreshFromDisk();
			broadcastNewProject(projectName);
		}
		else {
			Notification.show("Could not clone project from Git :(");
		}
	}
	
	private void broadcastNewProject(String projectName) {
		LobbyBroadcaster.broadcastProjectsChanged();
		LobbyPanel.getLobbyChat().addLine(user.getName()+" created project "+projectName);
	}

	/**
	 * Shows removetion dialog and if confirmed, then removes the project and
	 * directory.
	 * 
	 * @param projectName
	 *            the name of the project to be destroyer
	 */
	public void removeProject(String projectName) {
		RemoveProjectWindow window = new RemoveProjectWindow(user, projectName);
		UI.getCurrent().addWindow(window);
	}

	/**
	 * Opens MideaasEditor.
	 * 
	 * @param projectName
	 *            of the project to be opened
	 */
	public void openMideaasEditor(String projectName) {

		SharedProject project = SharedProject.getProject(projectName);
		project.addUser(user);
		
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
		setContent(editor);
		
		LobbyPanel.getLobbyChat().addLine(user.getName() + " opened " + projectName);
		LobbyBroadcaster.broadcastProjectsChanged();
	}

	public void logout() {
		if (user != null) {
			SharedProject.removeFromProjects(user);
			LobbyPanel.getLobbyChat().addLine(user.getName() + " logged out");
			user = null;
			drawLobby();
		}
	}

	public void loggedIn(User user) {
		this.user = user;
		drawLobby();
		LobbyPanel.getLobbyChat().addLine(user.getName() + " logged in");
	}

	public void uploadProject(File file) {
		try {
			String name = ZipUtils.projectNameInZip(file);
			if (SharedProject.projectExists(name)) {
				Notification.show("Project "+ name +" already exists. Please remove it first if you want to upload this.", Notification.Type.ERROR_MESSAGE);
			}
			
			File projectsRootDir = new File(
					MideaasConfig.getProperty(Prop.PROJECTS_DIR));
			File projectDir = ZipUtils.unzip(file, projectsRootDir);
			SharedProject.addProjectFromFiles(projectDir);
			broadcastNewProject(projectDir.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Notification.show("Unzipping failed :(");
		}
	}

	@Override
	public void closeRequested(SharedProject project) {
		project.removeFromProject(user);
		LobbyBroadcaster.broadcastProjectsChanged();
		setContent(mainLayout);
		drawLobby();
	}
}
