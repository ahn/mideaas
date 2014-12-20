package org.vaadin.mideaas.ide;

import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.editor.MultiUserDoc;

public interface IdeProjectCustomizer {


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
	 * @param project
	 * @return
	 */
	AsyncErrorChecker getErrorCheckerFor(String filename, IdeProject project);

	void docCreated(String id, MultiUserDoc doc);
	

}
