package org.vaadin.mideaas.ide;

import org.vaadin.mideaas.ide.model.SharedProject;

import com.vaadin.ui.MenuBar;

/** A plugin for MideaasEditor.
 * 
 * {@link #extendMenu(MenuBar)} can put items into the editor menu.
 * 
 */
public interface MideaasEditorPlugin {
	public void extendMenu(MenuBar menuBar, SharedProject project);
}
