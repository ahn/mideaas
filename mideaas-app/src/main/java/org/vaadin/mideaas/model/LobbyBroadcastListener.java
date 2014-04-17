package org.vaadin.mideaas.model;

import java.util.Set;
import java.util.TreeSet;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving lobbyBroadcast events.
 * The class that is interested in processing a lobbyBroadcast
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addLobbyBroadcastListener<code> method. When
 * the lobbyBroadcast event occurs, that object's appropriate
 * method is invoked.
 *
 * @see LobbyBroadcastEvent
 * https://vaadin.com/wiki/-/wiki/Main/Broadcasting%20messages%20to%20other%20users
 */
public interface LobbyBroadcastListener {
	public void projectsChanged();

	//public void loggedInUsersChanged(Set<User> users);
	//public void loggedInUsersChanged(TreeSet<User> users);
	public void loggedInUsersChanged(TreeSet<User> users, User user, boolean loggedin);
}
