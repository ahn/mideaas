package org.vaadin.mideaas.editor;

/**
 * How the users' edits are applied to the shared document.
 * TODO: currently not used anywhere...
 */
public enum SyncMode {
	
	/**
	 * Synchronize automatically as soon as possible.
	 */
	ASAP,
	
	/**
	 * Synchronization is done manually by the user.
	 */
	MANUAL

}
