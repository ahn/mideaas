package org.vaadin.mideaas.ide;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.vaadin.chatbox.SharedChat;
import org.vaadin.mideaas.editor.Team;

public class IdeProject {
	
	public interface Listener {
		public void changed(Collection<String> docNames);
	}
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	
	private final String name;
	
	private final HashMap<String, IdeDoc> docs = new HashMap<String, IdeDoc>();

	private final Team team = new Team();
	
	private final SharedChat chat = new SharedChat();
	
	public IdeProject(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public synchronized IdeDoc getDoc(String name) {
		return docs.get(name);
	}
	
	public IdeDoc putDoc(String name, IdeDoc doc) {
		IdeDoc d;
		synchronized(this) {
			d = docs.put(name, doc);
		}
		fireChanged();
		return d;
	}
	
	public IdeDoc removeDoc(String name) {
		IdeDoc d;
		synchronized (this) {
			d = docs.remove(name);	
		}
		if (d != null) {
			fireChanged();
		}
		return d;
	}
	
	public synchronized Collection<String> getDocNames() {
		return new TreeSet<String>(docs.keySet());
	}
	
	public synchronized void addListener(Listener li) {
		listeners.add(li);
	}
	
	public synchronized void removeListener(Listener li) {
		listeners.remove(li);
	}

	private void fireChanged() {

		Collection<String> docNames = getDocNames();
		
		for (Listener li : listeners) {
			li.changed(docNames);
		}
	}

	public SharedChat getChat() {
		return chat;
	}

}
