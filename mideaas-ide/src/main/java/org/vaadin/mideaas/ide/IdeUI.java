package org.vaadin.mideaas.ide;

import java.util.Map;

import org.vaadin.mideaas.editor.EditorUser;

import com.vaadin.annotations.Push;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;


@SuppressWarnings("serial")
@Push
public class IdeUI extends UI {

	private final Navigator navigator = new Navigator(this, this);
	private final IdeConfiguration config;
	private String afterLoginNavigateTo = "lobby";

	private static final ProjectContainer projects = new ProjectContainer();

	public IdeUI(IdeConfiguration config) {
		super();
		this.config = config == null ? new DefaultIdeConfiguration() : config;
	}
	
	@Override
	protected void init(VaadinRequest request) {
		navigator.addProvider(new IdeViewProvider(this, projects, config));
		navigator.setErrorView(new IdeErrorView());
		navigator.addView("stats", new StatsView(projects));
		getSession().getSession().setMaxInactiveInterval(300);
		
	}
	
	IdeCustomizer getIdeCustomizer() {
		return config.getIdeCustomizer();
	}

	/**
	 * Starts a project.
	 * 
	 * @param projectName
	 * @param projectFileContents key:filename, 
	 */
	public void startProject(String projectName, Map<String, String> projectFileContents) {
		IdeProject project = IdeUtil.createProject(projectName, projectFileContents, config);
		projects.putProject(project.getId(), project);
		navigator.navigateTo(project.getId());
	}
	
	public void logIn(IdeUser user) {
		setIdeUser(user);
		navigator.navigateTo(afterLoginNavigateTo);
	}
	
	public void logOut() {
		removeIdeUser();
		super.close();
		navigator.navigateTo("");
	}

	private void setIdeUser(IdeUser user) {
		VaadinSession s = getSession();
		s.setAttribute("user", user);
	}
	
	public IdeUser getIdeUser() {
		VaadinSession s = getSession();
		return (IdeUser) s.getAttribute("user");
	}

	private void removeIdeUser() {
		VaadinSession s = getSession();
		s.setAttribute("user", null);
	}
	
	@Override
	public void detach() {
		int n = getSession().getUIs().size();
		System.out.println("Before detaching " + this + " there are " + n + " UIs left in session " + getSession());
		if (n < 1) {
			IdeUser user = getIdeUser();
			if (user != null) {
				cleanup(user);
			}
		}
		super.detach();
	}

	private void cleanup(IdeUser user) {
		// TODO something or nothing?
	}

	public EditorUser getEditorUser() {
		IdeUser user = getIdeUser();
		return user==null ? null : user.getEditorUser();
	}

	void setNextNavigation(String where) {
		afterLoginNavigateTo = where;
	}

	

	

	
		
}
