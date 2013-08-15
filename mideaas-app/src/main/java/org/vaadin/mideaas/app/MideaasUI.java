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
import org.vaadin.mideaas.model.LobbyBroadcastListener;
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

// TODO: Auto-generated Javadoc
/**
 * The Class MideaasUI.
 */
@PreserveOnRefresh
@SuppressWarnings("serial")
@Theme("reindeer")
@Push
public class MideaasUI extends UI implements LobbyBroadcastListener, MideaasEditor.CloseHandler {

	/** The user. */
	private User user;

	/** The main layout. */
	private final VerticalLayout mainLayout = new VerticalLayout();

	static {
		applyConfig();
	}

	public class DownloadUI extends UI {

		@Override
		protected void init(VaadinRequest request) {
			setContent(new Label("DownloadProject"));
		}

	}

	/**
	 * Apply config.
	 */
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
	public void attach() {
		super.attach();
		mainLayout.setSizeFull();
	}

	@Override
	protected void init(VaadinRequest request) {
		drawLobby();
		LobbyBroadcaster.register(this);
	}

	@Override
	public void detach() {
		logout();
		LobbyBroadcaster.unregister(this);
		super.detach();
	}

	/*
	 * (non-Javadoc) If component has registered as a listener, then this
	 * function eceives broadcasts. If user has logged in, then lobby is redrawn
	 * and a notification is shown
	 * 
	 * @see
	 * org.vaadin.mideaas.app.LobbyBroadcastListener#receiveLobbyBroadcast(java
	 * .lang.String)
	 */
	@Override
	public void receiveLobbyBroadcast(final String message) {
		access(new Runnable() {
			@Override
			public void run() {
				if (user != null) {
					drawLobby();
					Notification.show(message,
							Notification.Type.HUMANIZED_MESSAGE);
				}
			}
		});
	}

	/**
	 * Draws lobby if user has logged in.
	 */
	private void drawLobby() {
		// user is collaborating in one of the projects
		if (user != null && SharedProject.isInProject(user)) {
			return;
		} else {
			// if not, then components are removed and redrawn
			mainLayout.removeAllComponents();
			this.setContent(mainLayout);
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
	}

	private Component createLoginPanel() {
		if (MideaasConfig.isExperiment()) {
			return new ExperimentLoginPanel(this);
		}
		else {
			return new LoginPanel(this, user);
		}
	}

	/**
	 * Creates new project and returns false if fails.
	 * 
	 * @param projectName
	 *            the project name
	 * @param type
	 *            the type
	 * @param createSkeleton
	 *            the create skeleton
	 * @return true, if successful
	 */
	public boolean newProject(String projectName, Boolean createSkeleton) {
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

			LobbyBroadcaster.broadcast(user.getName() + " created project: "
					+ projectName);
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
			LobbyBroadcaster.broadcast(user.getName() + " created project: " + projectName);
		}
		else {
			Notification.show("Could not clone project from Git :(");
		}

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
		
		
		MideaasEditor editor = new MideaasEditor(user, project, settings, plugins);
		editor.setTestingEnabled(!MideaasConfig.isExperiment());
		editor.setCloseHandler(this);
		setContent(editor);
		LobbyBroadcaster.broadcast(user.getName() + " entered to project: " + projectName + "!");
	}

	/**
	 * Logs user out from the system.
	 */
	public void logout() {
		if (user != null) {
			SharedProject.removeFromProjects(user);
			String name = user.getName();
			user = null;
			drawLobby();
			LobbyBroadcaster.broadcast(name + " logged out!");
		}
	}

	/**
	 * Logs user in to system.
	 * 
	 * @param user
	 *            the user
	 */
	public void loggedIn(User user) {
		this.user = user;
		LobbyBroadcaster.broadcast(user.getName() + " logged in!");
	}

	/**
	 * Upload project file.
	 * 
	 * @param file
	 *            the file
	 */
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
			LobbyBroadcaster.broadcast(user.getName() + " created project: "
					+ projectDir.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Notification.show("Unzipping failed :(");
		}
	}

	@Override
	public void closeRequested(SharedProject project) {
		project.removeFromProject(user);
		drawLobby();
	}
}
