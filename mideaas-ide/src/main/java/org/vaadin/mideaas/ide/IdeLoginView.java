package org.vaadin.mideaas.ide;

import com.vaadin.navigator.View;

/**
 * In the login view, a {@link IdeUser} is created.
 * 
 * After a successful login, {@link IdeLoginView} must
 * call {@link IdeUI#logIn(IdeUser)}.
 *
 */
public interface IdeLoginView extends View {}
