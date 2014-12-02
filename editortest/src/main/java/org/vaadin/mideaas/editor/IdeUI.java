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


@Theme("mideaas")
@SuppressWarnings("serial")
@Push
public class IdeUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = IdeUI.class, widgetset = "org.vaadin.mideaas.editor.AppWidgetSet", heartbeatInterval=5, closeIdleSessions=true)
    public static class Servlet extends VaadinServlet {}

	private final Navigator navigator = new Navigator(this, this);

	private static final ProjectContainer projects = new ProjectContainer();

	@Override
	protected void init(VaadinRequest request) {	
		navigator.addView("", new WelcomeView());
		navigator.addProvider(new SimpleViewProvider(this, projects));
		navigator.addProvider(new ProjectViewProvider(this, projects));
		navigator.setErrorView(new IdeErrorView());
		
		getSession().getSession().setMaxInactiveInterval(300);
	}

	public void startProject(MultiUserProject project) {
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
		System.out.println("There are " + n + " UIs left...");
		if (n < 1) {
			IdeUser user = getIdeUser();
			if (user != null) {
				cleanup(user);
			}
		}
		
		super.detach();
		System.out.println("IdeUI.detached " + this);
		
		
		for (UI ui : getSession().getUIs()) {
			System.out.println(ui + " - " + ui.getId() + " - " + ui.getCaption());
		}
		
	}

	private void cleanup(IdeUser user) {
		// TODO Auto-generated method stub
		
	}

	public EditorUser getEditorUser() {
		IdeUser user = getIdeUser();
		return user==null ? null : user.getEditorUser();
	}
		
}
