package org.vaadin.mideaas.ide;

import java.util.Map;

import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

public interface ProjectCustomizer {

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

	/**
	 * Upwards guard decides whether a user-edited document is valid to be
	 * pushed upstream, to the shared document.
	 * 
	 * @param filename
	 * @return guard or null
	 */
	Guard getUpwardsGuardFor(String filename);

	/**
	 * Downwards guard is the opposite of upwards guard. See
	 * {@link #getUpwardsGuardFor(String)}
	 * 
	 * @param filename
	 * @return guard or null
	 */
	Guard getDownwardsGuardFor(String filename);

	/**
	 * Filter
	 * 
	 * @param filename
	 * @return guard
	 */
	Filter getFilterFor(String filename);

	/**
	 * Returns error checker for the file.
	 * 
	 * @param filename
	 * @return
	 */
	AsyncErrorChecker getErrorCheckerFor(String filename);

}
