package org.vaadin.mideaas.ide;

import javax.servlet.annotation.WebServlet;

import org.vaadin.mideaas.editor.EditorUser;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;


//@Theme("mideaas")
@SuppressWarnings("serial")
@Push
public class IdeUI extends UI
{

//    @WebServlet(value = "/*", asyncSupported = true)
//    @VaadinServletConfiguration(productionMode = false, ui = IdeUI.class, widgetset = "org.vaadin.mideaas.ide.AppWidgetSet", heartbeatInterval=5, closeIdleSessions=true)
//    public static class Servlet extends VaadinServlet {}

	private final Navigator navigator = new Navigator(this, this);

	private ProjectCustomizer projectCustomizer;
	private IdeCustomizer ideCustomizer;

	private static final ProjectContainer projects = new ProjectContainer();

	@Override
	protected void init(VaadinRequest request) {	
		projectCustomizer = createProjectCustomizer();
		ideCustomizer = createIdeCustomizer();
		
		navigator.addView("", new WelcomeView());
		navigator.addProvider(new SimpleViewProvider(this, projects));
		navigator.addProvider(new ProjectViewProvider(this, projects, getIdeCustomizer()));
		navigator.setErrorView(new IdeErrorView());
		
		getSession().getSession().setMaxInactiveInterval(300);
	}
	
	

	protected ProjectCustomizer createProjectCustomizer() {
		return new DefaultProjectCustomizer();
	}
	
	public ProjectCustomizer getProjectCustomizer() {
		return projectCustomizer;
	}
	
	protected IdeCustomizer createIdeCustomizer() {
		return new DefaultIdeCustomizer();
	}
	
	public IdeCustomizer getIdeCustomizer() {
		return ideCustomizer;
	}

	public void startProject(IdeProject project) {
		projects.putProject(project.getName(), project);
		navigator.navigateTo(project.getName());
	}

	public void setSessionUser(IdeUser user) {
		VaadinSession s = getSession();
		s.setAttribute("user", user);
	}
	
	public IdeUser getIdeUser() {
		VaadinSession s = getSession();
		return (IdeUser) s.getAttribute("user");
	}

	public void removeSessionUser() {
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



	public void reset() {
		super.close();
		navigator.navigateTo("/");
	}

	
		
}
