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

	private final UserSettings userSettings;
	
	public MideaasIdeCustomizer(UserSettings userSettings) {
		this.userSettings = userSettings;
	}
	
	@Override
	public List<Component> getSidebarComponents(IdeProject project, IdeUser user) {
		List<Component> components = new LinkedList<Component>();
		Builder builder = new Builder((VaadinProject) project, userSettings);
		components.add(new BuildComponent(builder, user));
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
