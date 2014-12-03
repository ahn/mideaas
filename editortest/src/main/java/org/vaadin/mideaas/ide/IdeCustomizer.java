package org.vaadin.mideaas.ide;

import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;

public interface IdeCustomizer {

	List<Component> getSidebarComponents(IdeProject project);

	void customizeMenuBar(MenuBar menuBar);
	
}
