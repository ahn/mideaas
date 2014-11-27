package org.vaadin.mideaas.editor;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class Team {
	
	public interface Listener {
		public void changed(List<EditorUser> users);
	}

	private TreeSet<EditorUser> users = new TreeSet<EditorUser>();
	private LinkedList<Listener> listeners = new LinkedList<Listener>();

	public synchronized void addUser(EditorUser user) {
		boolean added;
		synchronized (this) {
			added = users.add(user);
		}
		if (added) {
			fireChanged();
		}
	}
	
	public synchronized void removeUser(EditorUser user) {
		boolean removed;
		synchronized (this) {
			removed = users.remove(user);
		}
		if (removed) {
			fireChanged();
		}
	}
	
	public synchronized List<EditorUser> getUsers() {
		return new LinkedList<EditorUser>(users );
	}

	public synchronized void addListener(Listener li) {
		listeners.add(li);
	}
	
	public synchronized void removeListener(Listener li) {
		listeners.remove(li);
	}
	
	private void fireChanged() {
		List<EditorUser> usersCopy;
		synchronized (this) {
			usersCopy = new LinkedList<EditorUser>(users);
		}
		for (Listener li : listeners) {
			li.changed(usersCopy);
		}
	}
}
