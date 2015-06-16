package org.vaadin.mideaas.ide;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.Suggester;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.editor.DocDifference;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;
import org.vaadin.mideaas.editor.user.Team;

public class IdeProject {
	
	public interface Listener {
		public void changed(Collection<String> docNames);
		public void usersChanged(String filename, Set<EditorUser> users);
	}
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	
	private final String id;
	private final String name;
	private final IdeProjectCustomizer customizer;
	
	private final HashMap<String, IdeDoc> docs = new HashMap<String, IdeDoc>();
	private final HashMap<String, DifferingChangedListener> changeListeners = new HashMap<String, DifferingChangedListener>();
	private final HashMap<String, Set<EditorUser>> docUsers = new HashMap<String, Set<EditorUser>>();

	private final Team team = new Team();
	
	private final SharedChat chat = new SharedChat();

	public IdeProject(String id, String name, IdeProjectCustomizer customizer) {
		this.id = id;
		this.name = name;
		this.customizer = customizer;
	}
	
	public IdeProject(String id, String name) {
		this(id, name, null);
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public synchronized IdeDoc getDoc(String filename) {
		return docs.get(filename);
	}

	public IdeDoc putDoc(String filename, String content) {
		MultiUserDoc doc;
		if (customizer != null) {
			doc = customizedDoc(filename, content);
			customizer.docCreated(filename, doc);
		}
		else {
			doc = new MultiUserDoc(new AceDoc(content));
		}
		
		IdeDoc ideDoc = new IdeDoc(doc, AceMode.forFile(filename));
		IdeDoc prev;
		synchronized(this) {
			prev = docs.put(filename, ideDoc);
			initDocUsers(filename, doc);
		}
		
		fireChanged();
		return prev;
	}
	
	private MultiUserDoc customizedDoc(String filename, String content) {
		Guard upGuard = customizer.getUpwardsGuardFor(filename);
		Guard downGuard = customizer.getDownwardsGuardFor(filename);
		Filter filter = customizer.getFilterFor(filename);
		AsyncErrorChecker checker = customizer.getErrorCheckerFor(filename, this);
		return new MultiUserDoc(new AceDoc(content), filter, upGuard, downGuard, checker);
	}
	
	private void initDocUsers(final String filename, MultiUserDoc doc) {
		Set<EditorUser> empty = Collections.emptySet();
		docUsers.put(filename, empty);
		
		DifferingChangedListener dcl = new DifferingChangedListener() {
			@Override
			public void differingChanged(Map<EditorUser, DocDifference> diffs) {
				diffChanged(filename, diffs);
			}
		};
		
		changeListeners.put(filename, dcl);
		doc.addDifferingChangedListener(dcl);
	}
	
	private void diffChanged(String filename, Map<EditorUser, DocDifference> diffs) {
		Set<EditorUser> newSet = diffs.keySet();
		boolean changed = false;
		synchronized (this) {
			Set<EditorUser> oldSet = docUsers.get(filename);
			if (!newSet.equals(oldSet)) {
				docUsers.put(filename, diffs.keySet());
				changed = true;
			}
		}
		if (changed) {
			fireUsersChanged(filename, newSet);
		}
	}
	
	public Set<EditorUser> getUsersOf(String filename) {
		Set<EditorUser> us = docUsers.get(filename);
		if (us == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(us);
	}

	private void removeChangeListener(String filename, MultiUserDoc doc) {
		DifferingChangedListener dcl = changeListeners.remove(filename);
		if (dcl != null) {
			doc.removeDifferingChangedListener(dcl);
		}
	}
	
	public IdeDoc removeDoc(String filename) {
		IdeDoc d;
		synchronized (this) {
			d = docs.remove(filename);
			if (d != null) {
				removeChangeListener(filename, d.getDoc());
			}
		}
		if (d != null) {
			fireChanged();
		}
		return d;
	}
	
	public synchronized Collection<String> getDocIds() {
		return new TreeSet<String>(docs.keySet());
	}
	
	public synchronized void addListener(Listener li) {
		listeners.add(li);
	}
	
	public synchronized void removeListener(Listener li) {
		listeners.remove(li);
	}

	private void fireChanged() {
		Collection<String> docNames = getDocIds();
		for (Listener li : listeners) {
			li.changed(docNames);
		}
	}
	
	private void fireUsersChanged(String filename, Set<EditorUser> users) {
		Collection<String> docNames = getDocIds();
		for (Listener li : listeners) {
			li.usersChanged(filename, users);
		}
	}

	public SharedChat getChat() {
		return chat;
	}

	/**
	 * can be overridden
	 */
	public void destroy() {
		// Nothing for now...
	}
	
	public synchronized IdeProjectSnapshot getSnapshot() {
		Map<String, String> snap = new TreeMap<String, String>();
		for (Entry<String, IdeDoc> e : docs.entrySet()) {
							 // That's a lot of dots...
			String content = e.getValue().getDoc().getBase().getDoc().getText();
			snap.put(e.getKey(), content);
		}
		return new IdeProjectSnapshot(snap);
	}
	
	/**
	 * can be overridden
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	public Suggester createSuggesterFor(String id, IdeUser user) {
		return null;
	}

	public static String randomProjectId() {
		return UUID.randomUUID().toString().substring(0,8);
	}
	
}
