package org.vaadin.mideaas.app;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.mideaas.app.java.VaadinProject;
import org.vaadin.mideaas.ide.IdeCustomizer;
import org.vaadin.mideaas.ide.IdeProject;
import org.vaadin.mideaas.ide.IdeUser;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class MideaasIdeCustomizer implements IdeCustomizer {

	@Override
	public List<Component> getSidebarComponents(IdeProject project, IdeUser user) {
		List<Component> components = new LinkedList<Component>();
		components.add(new MockBuildComponent(project));
		components.add(new BuildComponent(new Builder((VaadinProject) project, null), user));
		return components;
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
