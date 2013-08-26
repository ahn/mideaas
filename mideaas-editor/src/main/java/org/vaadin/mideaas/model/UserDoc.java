package org.vaadin.mideaas.model;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.model.UserDoc.Listener.ChangeType;

public class UserDoc {
	
	public interface Listener {
		public enum ChangeType {
			FROM_EDITOR,
			FROM_MUD
		}
		void changed(AceDoc doc, ChangeType type);
	}

	private List<Listener> listeners = new ArrayList<Listener>();
	
	private final User user;
	private AceDoc shadow;
	private AceDoc work;
	private MultiUserDoc mud;
	
	public UserDoc(User user, AceDoc initial) {
		this.user = user;
		shadow = initial;
		work = initial;
		
	}

	public synchronized void addListener(Listener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private synchronized List<Listener> getListeners() {
        return new ArrayList<Listener>(listeners);
    }

	
	public synchronized void setMUD(MultiUserDoc mud) {
		this.mud = mud;
	}
	
	public void editorChanged(AceDoc doc) {
		synchronized (this) {
//			debugPrint("editorChanged", doc);
			this.work = doc;
			ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, doc);
			boolean applied = mud.tryToApply(diff, user);
			if (applied) {
				shadow = doc;
			}
		}
		fireChanged(doc, Listener.ChangeType.FROM_EDITOR);
	}
	
	public boolean baseChanged(AceDoc base, User byUser) {
		boolean sameAsBase;
		AceDoc newWork;
		synchronized (this) {
//			debugPrint("baseChanged", base);
			ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, base);
			shadow = base;
			newWork = work = diff.applyTo(work);
			sameAsBase = work.equals(base);
		}
		
		fireChanged(newWork, Listener.ChangeType.FROM_MUD);
		
		return sameAsBase;
	}
	
	@SuppressWarnings("unused")
	private synchronized void debugPrint(String s, AceDoc doc) {
		System.out.println("\n--- "+user.getName()+" --- " + s + " --- " + this + " - " +Thread.currentThread());
		System.out.println(doc.getText());
		System.out.println("SHADOW:\n"+shadow.getText());
	}

	private void fireChanged(final AceDoc doc, final ChangeType type) {
		final List<Listener> lis = getListeners();
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (Listener li : lis) {
					li.changed(doc, type);
				}
			}
		}).run();
		
	}
	
	public User getUser() {
		// No need to sync because final.
		return user;
	}

	public synchronized void setDoc(AceDoc doc) {
		work = doc;
		ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, doc);
		boolean applied = mud.tryToApply(diff, null /* XXX */);
		if (applied) {
			shadow = doc;
		}
	}

	public synchronized AceDoc getDoc() {
		return work;
	}

	
}
