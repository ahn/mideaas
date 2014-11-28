package org.vaadin.mideaas.editor;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;


@Theme("runo")
@SuppressWarnings("serial")
@Push
public class IdeUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = IdeUI.class, widgetset = "org.vaadin.mideaas.editor.AppWidgetSet", heartbeatInterval=5)
    public static class Servlet extends VaadinServlet {}

	private final Navigator navigator = new Navigator(this, this);

	private static final ProjectContainer projects = new ProjectContainer();

	@Override
	protected void init(VaadinRequest request) {	
		navigator.addView("", new WelcomeView());
		navigator.addProvider(new SimpleViewProvider(this, projects));
		navigator.addProvider(new ProjectViewProvider(this, projects));
		navigator.setErrorView(new IdeErrorView());
	}

	public void startProject(MultiUserProject project) {
		projects.putProject(project.getName(), project);
		navigator.navigateTo(project.getName());
	}

	public void setSessionUser(EditorUser user) {
		VaadinSession s = getSession();
		s.setAttribute("user-id", user.getId());
		s.setAttribute("user-name", user.getName());
		s.setAttribute("user-email", user.getEmail());
	}
	
	public EditorUser getSessionUser() {
		VaadinSession s = getSession();
		Object id = s.getAttribute("user-id");
		if (id == null) {
			return null;
		}
		return new EditorUser(
				(String)id,
				(String)s.getAttribute("user-name"),
				(String)s.getAttribute("user-email"));
	}

	public void removeSessionUser() {
		VaadinSession s = getSession();
		s.setAttribute("user-id", null);
		s.setAttribute("user-name", null);
		s.setAttribute("user-email", null);
	}
	
	@Override
	public void detach() {
		System.out.println("IdeUI.detach " + this);
		
		super.detach();
	}
		
}
