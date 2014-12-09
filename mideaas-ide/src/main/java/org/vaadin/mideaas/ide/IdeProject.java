package org.vaadin.mideaas.ide;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.Suggester;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.Team;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

public class IdeProject {
	
	public interface Listener {
		public void changed(Collection<String> docNames);
	}
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	
	private final String id;
	private final String name;
	private final IdeProjectCustomizer customizer;
	
	private final HashMap<String, IdeDoc> docs = new HashMap<String, IdeDoc>();

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
	
	public synchronized IdeDoc getDoc(String id) {
		return docs.get(id);
	}
//	
//	public IdeDoc putDoc(String id, IdeDoc doc) {
//		IdeDoc d;
//		synchronized(this) {
//			d = docs.put(id, doc);
//		}
//		fireChanged();
//		return d;
//	}
	
	public IdeDoc putDoc(String id, String content) {
		MultiUserDoc doc;
		if (customizer != null) {
			doc = customizedDoc(id, content);
		}
		else {
			doc = new MultiUserDoc(new AceDoc(content));
		}
		IdeDoc ideDoc = new IdeDoc(doc, AceMode.forFile(id));
		IdeDoc prev;
		synchronized(this) {
			prev = docs.put(id, ideDoc);
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
	
	public IdeDoc removeDoc(String id) {
		IdeDoc d;
		synchronized (this) {
			d = docs.remove(id);	
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

	public SharedChat getChat() {
		return chat;
	}

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

//	public ProjectCustomizer getCustomizer() {
//		return customizer;
//	}

	public Suggester createSuggesterFor(String id) {
		if (customizer != null) {
			return customizer.getSuggesterFor(id, this);
		}
		return null;
	}

}
