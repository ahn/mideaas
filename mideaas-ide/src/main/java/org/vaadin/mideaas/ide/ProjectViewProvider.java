package org.vaadin.mideaas.ide;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

@SuppressWarnings("serial")
public final class ProjectViewProvider implements ViewProvider {

	private final IdeUI ui;
	private final ProjectContainer projects;
	private final IdeCustomizer cust;
	
	public ProjectViewProvider(IdeUI ui, ProjectContainer projects, IdeCustomizer cust) {
		this.ui = ui;
		this.projects = projects;
		this.cust = cust;
	}

	@Override
	public String getViewName(String vap) {
		return vap.length() > 0 && vap.indexOf("/") == -1 ? vap : null;
	}

	@Override
	public View getView(String viewName) {

		IdeProject project = projects.getProject(viewName);
		
		if (project == null) {
			return null;
		}
		
		IdeUser user = ui.getIdeUser();
		if (user==null) {
			return new WelcomeView(project);
		}
		
		return new IdeView(project, user, cust);

	}
}