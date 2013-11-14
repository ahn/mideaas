package org.vaadin.mideaas.app;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

@SuppressWarnings("serial")
public class EditorViewProvider implements ViewProvider {

	private static final String PREFIX = "edit/";
	private MideaasUI ui;
	
	public EditorViewProvider(MideaasUI ui) {
		this.ui = ui;
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
				return new EditorView(projectName, ui);
			}
			else {
				return new LoginView(ui, viewName);
			}
		}
		return null;
	}

}
