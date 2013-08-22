package org.vaadin.mideaas.app;

import org.vaadin.mideaas.frontend.MideaasEditorPlugin;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class TestPlugin implements MideaasEditorPlugin {

	private Window testWindow;
	
	@Override
	public void extendMenu(MenuBar menuBar) {
		MenuItem root = menuBar.addItem("Tests", null);
		root.addItem("Run tests...", createRunTestCommand());
	}

	@SuppressWarnings("serial")
	private Command createRunTestCommand() {
		return new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				if (testWindow==null || !testWindow.isAttached()) {
					UI.getCurrent().addWindow(getTestWindow());
				}
			}
		};
	}

	private Window getTestWindow() {
		if (testWindow==null) {
			testWindow = new Window("Tests");
			testWindow.setWidth("80%");
			testWindow.setHeight("80%");
			testWindow.center();
			testWindow.setContent(new MideaasTest("Test"));
		}
		return testWindow;
	}
}
