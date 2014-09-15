package org.vaadin.mideaas.app;

import org.vaadin.mideaas.frontend.MideaasEditorPlugin;
import org.vaadin.mideaas.model.UserSettings;
import org.vaadin.mideaas.model.SharedProject;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

public class SettingsPlugin implements MideaasEditorPlugin {
	
	private final UserSettings settings;

	public SettingsPlugin(UserSettings settings) {
		this.settings = settings;
	}

	@Override
	public void extendMenu(MenuBar menuBar, SharedProject project) {
		MenuItem root = menuBar.addItem("Settings", null);
		root.addItem("Widgetset", createWidgetSetCommand());
	}

	@SuppressWarnings("serial")
	private Command createWidgetSetCommand() {
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				UI.getCurrent().addWindow(new WidgetsetSettingsWindow(settings));
			}
		};
	}

}
