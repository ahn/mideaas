package org.vaadin.mideaas.ide;

import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;

public class DefaultIdeCustomizer implements IdeCustomizer {

	@Override
	public List<Component> getSidebarComponents(IdeProject project) {
		return null;
	}

	@Override
	public void customizeMenuBar(MenuBar menuBar) {
		
	}

}
