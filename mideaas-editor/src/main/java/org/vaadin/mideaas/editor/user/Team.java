package org.vaadin.mideaas.editor.user;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.vaadin.mideaas.editor.EditorUser;

public class Team {
	
	public interface Listener {
		public void changed(List<EditorUser> users);
	}

	//private TreeSet<EditorUser> users = new TreeSet<EditorUser>();
	private TreeMap<EditorUser, Integer> users = new TreeMap<EditorUser, Integer>();
	private LinkedList<Listener> listeners = new LinkedList<Listener>();

	public synchronized void addUser(EditorUser user) {
		boolean changed = false;
		synchronized (this) {
			Integer n = users.get(user);
			if (n == null) {
				users.put(user, 1);
				changed = true;
			}
			else {
				users.put(user, n+1);
			}
			System.out.println("addUser " + user.getId() + " " + n + " -> " + users.get(user));
		}
		if (changed) {
			fireChanged();
		}
	}
	
	public synchronized void removeUser(EditorUser user) {
		boolean changed = false;
		synchronized (this) {
			Integer n = users.get(user);
			if (n != null) {
				if (n <= 1) {
					users.remove(user);
					changed = true;
				}
				else {
					users.put(user, n-1);
				}
			}
			System.out.println("removeUser " + user.getId() + " " + n + " -> " + users.get(user));
		}
		if (changed) {
			fireChanged();
		}
	}
	
	public synchronized List<EditorUser> getUsers() {
		return new LinkedList<EditorUser>(users.keySet());
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
			usersCopy = getUsers();
		}
		for (Listener li : listeners) {
			li.changed(usersCopy);
		}
	}
}
