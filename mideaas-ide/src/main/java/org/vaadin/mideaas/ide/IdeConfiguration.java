package org.vaadin.mideaas.ide;

import java.util.Map;


public interface IdeConfiguration {
	public ProjectCustomizer getProjectCustomizer(IdeProject project);
	
	/**
	 * Called when the {@link Ide} has been created.
	 * 
	 * This method can customize the Ide. See e.g
	 * {@link Ide#addSideBarComponents(java.util.List)},
	 * {@link Ide#getMenuBar()}
	 */
	public void ideCreated(Ide ide);
	
	/**
	 * The view that's shown when the user hasn't logged in.
	 * See {@link IdeLoginView}.
	 */
	public IdeLoginView createLoginView();
	
	/**
	 * The view that's shown when the user has logged in.
	 * In the {@link IdeLobbyView} the user selects a project to edit.
	 * See {@link IdeLobbyView}.
	 */
	public IdeLobbyView createLobbyView();

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
	public IdeProject createProject(String id, String name, Map<String, String> files);

	public DocAdderComponent createDocAdderComponent();
}
