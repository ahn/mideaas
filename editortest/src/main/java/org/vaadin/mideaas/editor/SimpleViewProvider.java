package org.vaadin.mideaas.editor;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

@SuppressWarnings("serial")
public final class SimpleViewProvider implements ViewProvider {
	
	private final IdeUI ui;
	private final ProjectContainer projects;
	
	public SimpleViewProvider(IdeUI ui, ProjectContainer projects) {
		this.ui = ui;
		this.projects = projects;
	}

	@Override
	public String getViewName(String vap) {
		return vap.split("/").length == 2 ? vap : null;
	}

	@Override
	public View getView(String viewName) {
		System.out.println("SimpleViewProvider getView "+ viewName);
		String[] ss = viewName.split("/", 2);
		if (ss.length != 2 ) {
			return null;
		}
		
		MultiUserProject project = projects.getProject(ss[0]);
		if (project == null) {
			return null;
		}
		
		IdeDoc doc = project.getDoc(ss[1]);
		if (doc == null) {
			return null;
		}
		
		EditorUser user = ui.getSessionUser();
		if (user==null) {
			return new WelcomeView(project, ss[1]);
		}
		
		return new SimpleView(doc, user,  ss[1] + " (" + ss[0] + ")");
	}
}