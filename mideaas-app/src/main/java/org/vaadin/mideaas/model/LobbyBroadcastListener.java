package org.vaadin.mideaas.model;

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
	
	/**
	 * Detach.
	 */
	public void detach();
	
	/**
	 * Receive lobby broadcast.
	 *
	 * @param message the message
	 */
	public void receiveLobbyBroadcast(final String message);	 
}
