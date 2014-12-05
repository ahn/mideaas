package org.vaadin.mideaas.ide;

import java.util.Map;


public class DefaultIdeConfiguration implements IdeConfiguration {
	
	@Override
	public ProjectCustomizer getProjectCustomizer(IdeProject project) {
		return new DefaultProjectCustomizer();
	}

	@Override
	public IdeCustomizer getIdeCustomizer() {
		return new DefaultIdeCustomizer();
	}
	
	@Override
	public IdeLoginView createLoginView() {
		return new JustUsernameLoginView();
	}

	@Override
	public IdeLobbyView createLobbyView() {
		return new ExampleProjectLobbyView();
	}
	
	@Override
	public IdeProject createProject(String id, String name, Map<String, String> files) {
		return new IdeProject(id, name);
	}

}
