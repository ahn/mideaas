package org.vaadin.mideaas.ide;

import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;

public class DefaultIdeCustomizer implements IdeCustomizer {

	@Override
	public List<Component> getSidebarComponents(IdeProject project, IdeUser user) {
		return null;
	}
	
	@Override
	public Component getBelowEditorComponent(IdeProject project, IdeUser user) {
		return null;
	}

	@Override
	public void customizeMenuBar(MenuBar menuBar) {
		// Nothing...
	}

	

}
