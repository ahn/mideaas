package org.vaadin.mideaas.ide;

import java.util.Map;


public class DefaultIdeConfiguration implements IdeConfiguration {

	private static final String API_KEY = "97a7e251c538106e7922";
	private static final String API_SECRET = "6a36b0992e5e2b00a38c44c21a6e0dc8ae01d83b";
	
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
		return new GitHubLoginView(API_KEY, API_SECRET);
	}

	@Override
	public IdeLobbyView createLobbyView() {
		return new GitHubLobbyView(API_KEY, API_SECRET);
	}
	
	@Override
	public IdeProject createProject(String id, String name, Map<String, String> files) {
		return new IdeProject(id, name);
	}

}
