package org.vaadin.mideaas.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// TODO: Auto-generated Javadoc
/**
 * The Class LobbyBroadcaster.
 * https://vaadin.com/wiki/-/wiki/Main/Broadcasting%20messages%20to%20other%20users
 */
public class LobbyBroadcaster {

    /** The listeners. */
    private static final Collection<LobbyBroadcastListener> listeners = new ArrayList<LobbyBroadcastListener>();
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();
    
    /**
     * Registers LobbyBroadcastListener to listen new broadcasts.
     *
     * @param listener the listener
     */
    public synchronized static void register(LobbyBroadcastListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters LobbyBroacastListener -> no more broadcasts to you.
     *
     * @param listener the listener
     */
    public synchronized static void unregister(LobbyBroadcastListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gets the list of LobbyBroadcastListeners.
     *
     * @return the listeners
     */
    private synchronized static Collection<LobbyBroadcastListener> getListeners() {
    	Collection<LobbyBroadcastListener> listenerCopy = new ArrayList<LobbyBroadcastListener>();
        listenerCopy.addAll(listeners);
        return listenerCopy;
    }
	
	public static void broadcastProjectsChanged() {
		// Make a copy of the listener list while synchronized, can't be
        // synchronized while firing the event or we would have to fire each
        // event in a separate thread.
        final Collection<LobbyBroadcastListener> listenersCopy = getListeners();
        
        // We spawn another thread to avoid potential deadlocks with
        // multiple UIs locked simultaneously
        pool.submit(new Runnable() {
            @Override
            public void run() {
                for (LobbyBroadcastListener listener : listenersCopy) {
                    listener.projectsChanged();
                }
            }
        });
	}

	/*
	public static void broadcastLoggedInUsersChanged(final Set<User> users) {
        final Collection<LobbyBroadcastListener> listenersCopy = getListeners();
        pool.submit(new Runnable() {
            @Override
            public void run() {
                for (LobbyBroadcastListener listener : listenersCopy) {
                    listener.loggedInUsersChanged(users);
                }
            }
        });
	}
	*/
	
	public static void broadcastLoggedInUsersChanged(final TreeSet<User> users, final User user, final boolean loggedin) {
        final Collection<LobbyBroadcastListener> listenersCopy = getListeners();
        pool.submit(new Runnable() {
            @Override
            public void run() {
                for (LobbyBroadcastListener listener : listenersCopy) {
                    listener.loggedInUsersChanged(users, user, loggedin);
                }
            }
        });
	}
	
}