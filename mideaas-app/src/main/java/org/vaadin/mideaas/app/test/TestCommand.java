package org.vaadin.mideaas.app.test;

import org.vaadin.mideaas.app.VaadinProject;

import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class TestCommand implements Command {
	
	private final VaadinProject project;
	private  Window testWindow;
	
	public TestCommand(VaadinProject project) {
		this.project = project;
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		if (testWindow==null || !testWindow.isAttached()) {
			UI.getCurrent().addWindow(getTestWindow());
		}
	}
	
	private Window getTestWindow() {
		if (testWindow==null) {
			//SharedProject project = super.getClass();
			testWindow = new Window("Tests");
			testWindow.setWidth("80%");
			testWindow.setHeight("80%");
			testWindow.center();
			testWindow.setContent(new MideaasTest("Test", project));
		}
		return testWindow;
	}

}
