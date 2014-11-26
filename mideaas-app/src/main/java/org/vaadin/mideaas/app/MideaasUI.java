package org.vaadin.mideaas.app;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.vaadin.mideaas.app.MideaasConfig.Prop;
import org.vaadin.mideaas.app.model.GitRepository;
import org.vaadin.mideaas.app.model.LobbyBroadcaster;
import org.vaadin.mideaas.app.model.ZipUtils;
import org.vaadin.mideaas.ide.ClaraEditor;
import org.vaadin.mideaas.ide.JettyUtil;
import org.vaadin.mideaas.ide.MavenUtil;
import org.vaadin.mideaas.ide.model.ProjectLog;
import org.vaadin.mideaas.ide.model.SharedProject;
import org.vaadin.mideaas.ide.model.User;
import org.vaadin.mideaas.ide.model.UserSettings;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;


@PreserveOnRefresh
@SuppressWarnings("serial")
@Theme("mideaas")
@Push
public class MideaasUI extends UI {
	
	@WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MideaasUI.class, widgetset = "org.vaadin.mideaas.app.AppWidgetSet", heartbeatInterval=5)
    public static class Servlet extends VaadinServlet {
    }

	private static TreeSet<User> loggedInUsers = new TreeSet<User>();
	static void addUser(User user) {
		synchronized (MideaasUI.class) {
			loggedInUsers.add(user);
		}
		LobbyBroadcaster.broadcastLoggedInUsersChanged(getLoggedInUsers(), user, true);
	}
	
	static void removeUser(User user) {
		synchronized (MideaasUI.class) {
			loggedInUsers.remove(user);
		}
		SharedProject.removeFromProjects(user);
		LobbyBroadcaster.broadcastLoggedInUsersChanged(getLoggedInUsers(), user, false);
	}
	
	synchronized static TreeSet<User> getLoggedInUsers() {
		return new TreeSet<User>(loggedInUsers);
	}
	
	private Navigator navigator;
	
	private User user;
	
	private final UserSettings settings = MideaasConfig.getDefaultUserSettings();

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

		MavenUtil.setMavenHome(MideaasConfig.getMavenHome());

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
		
		navigator = new Navigator(this, this);
		
		navigator.addView("", new LoginView(this, "lobby"));
		
		navigator.addView("lobby", new LobbyView(this));
		
		navigator.addProvider(new EditorViewProvider(this, settings));
		
		//navigator.setErrorView(new LobbyView(this));
		
		/*
		// cutomizing System messages
		VaadinService.getCurrent().setSystemMessagesProvider( new SystemMessagesProvider() {
		    
			@Override
			public SystemMessages getSystemMessages(
					SystemMessagesInfo systemMessagesInfo) {
				// TODO Auto-generated method stub
				CustomizedSystemMessages messages = new CustomizedSystemMessages();
				
				//messages.setSessionExpiredCaption("farshad");
				//messages.setSessionExpiredMessage("ahmadi");
				//messages.setSessionExpiredNotificationEnabled(false);
				//messages.setSessionExpiredURL("/mideaastest");
				//messages.setSessionExpiredURL("/mideaas");
				messages.setSessionExpiredNotificationEnabled(true);
				
		    	return messages;
			}
		});
		*/
	}

	@Override
	public void detach() {
		System.out.println(this + " MideaasUI.detach");
		super.detach();
		logout();
	}

	
	@Override
	public void attach() {
		super.attach();
	}
	
	public void logout() {
		setUser(null);
	}

	public User getUser() {
		return user;
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
			SharedProject project = SharedProject.createNewProject(projectName,settings);
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
		LobbyView.getLobbyChat().addLine(getUser().getName()+" created project "+projectName);
	}

	/**
	 * Shows removetion dialog and if confirmed, then removes the project and
	 * directory.
	 * 
	 * @param projectName
	 *            the name of the project to be destroyer
	 */
	public void removeProject(String projectName) {
		RemoveProjectWindow window = new RemoveProjectWindow(getUser(), projectName);
		UI.getCurrent().addWindow(window);
	}

	public void openMideaasEditor(String projectName) {
		navigator.navigateTo("edit/"+projectName);
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

	public void setUser(User user) {
		if (user==null ? this.user==null : user.equals(this.user)) {
			return;
		}
		if (this.user!=null) {
			removeUser(this.user);
		}
		this.user = user;
		if (user!=null) {
			addUser(user);
		}
		else {
			navigateTo("");
			//getSession().close();
			
		}
	}

	public void navigateTo(String nextView) {
		navigator.navigateTo(nextView);
	}

	
}
