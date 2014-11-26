package org.vaadin.mideaas.app;

import org.vaadin.mideaas.ide.model.UserSettings;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

@SuppressWarnings("serial")
public class EditorViewProvider implements ViewProvider {

	private static final String PREFIX = "edit/";
	private MideaasUI ui;
	private UserSettings settings;
	
	public EditorViewProvider(MideaasUI ui, UserSettings settings) {
		this.ui = ui;
		this.settings = settings;
	}
	
	@Override
	public String getViewName(String viewAndParameters) {
		return viewAndParameters;
	}

	@Override
	public View getView(String viewName) {
		if (viewName.startsWith("edit/")) {
			if (ui.getUser()!=null) {
				String projectName = viewName.substring(PREFIX.length());
				return new EditorView(projectName, ui, settings);
			}
			else {
				return new LoginView(ui, viewName);
			}
		}
		return null;
	}

}
