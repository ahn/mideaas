package org.vaadin.mideaas.editor;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

@SuppressWarnings("serial")
public final class ProjectViewProvider implements ViewProvider {

	private final IdeUI ui;
	private final ProjectContainer projects;
	
	public ProjectViewProvider(IdeUI ui, ProjectContainer projects) {
		this.ui = ui;
		this.projects = projects;
	}

	@Override
	public String getViewName(String vap) {
		return vap.length() > 0 && vap.indexOf("/") == -1 ? vap : null;
	}

	@Override
	public View getView(String viewName) {

		MultiUserProject project = projects.getProject(viewName);
		
		if (project == null) {
			return null;
		}
		
		IdeUser user = ui.getIdeUser();
		if (user==null) {
			return new WelcomeView(project);
		}
		
		return new IdeView(project, user);

	}
}