package org.vaadin.mideaas.app;

import org.vaadin.mideaas.frontend.MideaasEditorPlugin;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

public class DebugPlugin implements MideaasEditorPlugin {
	

	public DebugPlugin() {
	}

	@Override
	public void extendMenu(MenuBar menuBar) {
		MenuItem root = menuBar.addItem("Debug", null);
		root.addItem("Close session", createWidgetSetCommand());
	}

	@SuppressWarnings("serial")
	private Command createWidgetSetCommand() {
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				UI.getCurrent().getSession().close();
			}
		};
	}

}
