package org.vaadin.mideaas.ide;

import java.util.Map;


public interface IdeConfiguration {
	ProjectCustomizer getProjectCustomizer(IdeProject project);
	IdeCustomizer getIdeCustomizer();
	IdeLoginView createLoginView();
	IdeLobbyView createLobbyView();

	/**
	 * All the projects are created by calling this method.
	 * 
	 * NOTE: this method must return an empty project, NOT add any files.
	 * The files parameter is just given to help decide
	 * what kind of project to return based on the files.
	 * @param name
	 * @param files
	 * @return
	 */
	IdeProject createProject(String id, String name, Map<String, String> files);
}
