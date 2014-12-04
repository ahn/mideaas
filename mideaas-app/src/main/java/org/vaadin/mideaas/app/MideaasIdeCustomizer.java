package org.vaadin.mideaas.app;

import java.util.Collections;
import java.util.List;

import org.vaadin.mideaas.ide.IdeCustomizer;
import org.vaadin.mideaas.ide.IdeProject;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class MideaasIdeCustomizer implements IdeCustomizer {

	@Override
	public List<Component> getSidebarComponents(IdeProject project) {
		return Collections.singletonList((Component)new MockBuildComponent(project));
	}

	@Override
	public void customizeMenuBar(MenuBar menuBar) {
		MenuItem menu = menuBar.addItem("Moi", null);
		menu.addItem("foo", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				System.out.println("foofoofoo");
			}
		});
	}

}
