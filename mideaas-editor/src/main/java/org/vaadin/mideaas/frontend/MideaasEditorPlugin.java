package org.vaadin.mideaas.frontend;

import com.vaadin.ui.MenuBar;

/** A plugin for MideaasEditor.
 * 
 * {@link #extendMenu(MenuBar)} can put items into the editor menu.
 * 
 */
public interface MideaasEditorPlugin {
	public void extendMenu(MenuBar menuBar);
}
