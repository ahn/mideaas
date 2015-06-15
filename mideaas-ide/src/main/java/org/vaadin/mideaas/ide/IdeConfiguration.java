package org.vaadin.mideaas.ide;

import java.io.File;
import java.util.Map;


public interface IdeConfiguration {
	
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
	 * @param workDir - may be null if there is no work dir
	 * @return
	 */
	public IdeProject createProject(String id, String name, Map<String, String> files, File workDir);

	/**
	 * A component for adding new docs to project.
	 * @param project 
	 * @param docAdder 
	 */
	public DocAdderComponent createDocAdderComponent(IdeProject project, String suggestedParent, DocAdder docAdder);

	/**
	 * "Project files" are files that can be modified directly by user in the IDE.
	 * They are shown in the file list.
	 * 
	 * When a project is loaded from disk, only project files are included in the project.
	 */
	public boolean isProjectFile(String filename);

}
