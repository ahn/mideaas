package org.vaadin.mideaas.app;

import org.vaadin.mideaas.ide.DefaultIdeConfiguration;
import org.vaadin.mideaas.ide.IdeCustomizer;
import org.vaadin.mideaas.ide.ProjectCustomizer;

public class MideaasIdeConfiguration extends DefaultIdeConfiguration {

	@Override
	public ProjectCustomizer getProjectCustomizer() {
		return new MideaasProjectCustomizer();
	}

	@Override
	public IdeCustomizer getIdeCustomizer() {
		return new MideaasIdeCustomizer();
	}	

}
