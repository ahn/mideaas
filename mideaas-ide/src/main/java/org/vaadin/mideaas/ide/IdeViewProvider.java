package org.vaadin.mideaas.ide;

import java.util.regex.Matcher;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

@SuppressWarnings("serial")
public final class IdeViewProvider implements ViewProvider {

	private final IdeUI ui;
	private final ProjectContainer projects;
	private final IdeConfiguration config;
	
	public IdeViewProvider(IdeUI ui, ProjectContainer projects, IdeConfiguration config) {
		this.ui = ui;
		this.projects = projects;
		this.config = config;
	}

	@Override
	public String getViewName(String vap) {
		if (vap.isEmpty()) {
			return vap;
		}
		if (isProjectAddress(vap)) {
			return vap;
		}
		if (isFileAddress(vap)) {
			return vap;
		}
		return null;
	}

	private boolean isFileAddress(String vap) {
		return IdeUtil.RE_URL_FILE.matcher(vap).matches();
	}

	private boolean isProjectAddress(String vap) {
		return IdeUtil.RE_URL_PROJECT.matcher(vap).matches();
	}

	private boolean isLoggedIn() {
		return ui.getIdeUser() != null; // XXX diff kinda users...
	}

	@Override
	public View getView(String viewName) {
		if (!isLoggedIn()) {
			ui.setNextNavigation(viewName);
			return config.createLoginView();
		}
		
		if (viewName.isEmpty()) {
			return config.createLobbyView();
		}

		View v = tryToOpenProject(viewName);
		if (v != null) {
			return v;
		}
		
		return tryToOpenFile(viewName);
	}

	private View tryToOpenProject(String viewName) {
		Matcher m = IdeUtil.RE_URL_PROJECT.matcher(viewName);
		if (m.matches()) {
			String projectId = m.group(1);
			IdeProject project = projects.getProject(projectId);
			if (project != null) {
				return new IdeView(project, ui.getIdeUser(), config);
			}
		}
		return null;
	}

	private View tryToOpenFile(String viewName) {
		Matcher m = IdeUtil.RE_URL_FILE.matcher(viewName);
		if (m.matches()) {
			String projectId = m.group(1);
			String filename = m.group(2);
			IdeProject project = projects.getProject(projectId);
			if (project != null) {
				IdeDoc doc = project.getDoc(filename);
				if (doc != null) {
					String title = filename.substring(filename.lastIndexOf("/")+1);
					return new SimpleView(doc, ui.getIdeUser().getEditorUser(),  title);
				}
			}
		}
		return null;
	}
}