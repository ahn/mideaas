package org.vaadin.mideaas.ide;

import com.vaadin.navigator.View;

/**
 * In the lobby view, the user selects a project.
 * 
 * When done, {@link IdeLobbyView} must call {@link IdeUI#startProject(String, java.util.Map)}
 * to start a project.
 * 
 * When entering IdeLobbyView a user has already logged in at a {@link IdeLoginView}.
 * The logged in user can be get with {@link IdeUI#getIdeUser()}.
 *
 */
public interface IdeLobbyView extends View {}
