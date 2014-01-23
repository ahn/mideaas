package org.vaadin.mideaas.editor;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.MultiUserDoc.BaseChangedListener;

public class UserDoc implements BaseChangedListener {
	
	public interface Listener {
		public enum ChangeType {
			FROM_EDITOR,
			FROM_MUD,
			FROM_OTHER
		}
		void changed(AceDoc doc, ChangeType type);
	}

	private List<Listener> listeners = new ArrayList<Listener>();
	
	private final EditorUser user;
	private AceDoc shadow;
	private AceDoc work;
	private MultiUserDoc mud;

	private SyncMode syncMode = SyncMode.ASAP;

	private int attached;
	
	public UserDoc(EditorUser user, AceDoc initial) {
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
	
	public SyncMode getSyncMode() {
		return syncMode;
	}
	
	public void setSyncMode(SyncMode mode) {
		syncMode  = mode;
	}
	
	public void editorChanged(AceDoc doc) {
		if (syncMode==SyncMode.ASAP) {
			syncDoc(doc);
		}
		else if (syncMode==SyncMode.MANUAL) {
			syncDocNoToBase(doc);
		}
	}
	
	public void syncDoc(AceDoc doc) {
		AceDoc fireThis = setWorkDoc(doc);
		if (fireThis!=null) {
			fireChanged(fireThis, Listener.ChangeType.FROM_EDITOR);
		}
	}

	private void syncDocNoToBase(AceDoc doc) {
		AceDoc newWork = setWorkDocNoToBase(doc);
		fireChanged(newWork, Listener.ChangeType.FROM_EDITOR);
	}
	
	synchronized private AceDoc setWorkDoc(AceDoc doc) {
		this.work = doc;
		ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, doc);
		boolean applied = mud.tryToApply(diff, this);
		if (!applied) {
			return work;
		}
		return null;
	}
	
	synchronized private AceDoc setWorkDocNoToBase(AceDoc doc) {
		this.work = doc;
		ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, doc);
		mud.dontTryToApply(diff, this);
		return work;
	}
	
	public void updateShadow() {
		shadow = work;
	}
	
	@Override
	public void baseChanged(AceDoc base, EditorUser byUser) {
		AceDoc newWork;
		boolean workChanged;
		synchronized (this) {
			ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, base);
			shadow = base;
			newWork = diff.applyTo(work);
			workChanged = !work.equals(newWork);
		}
		
		if (workChanged) {
			fireChanged(newWork, Listener.ChangeType.FROM_MUD);
			if (syncMode==SyncMode.ASAP) {
				setWorkDoc(newWork);
			}
			else if (syncMode==SyncMode.MANUAL) {
				setWorkDocNoToBase(newWork);
			}
		}
	}

	private void fireChanged(final AceDoc doc, final Listener.ChangeType type) {
		for (Listener li : getListeners()) {
			li.changed(doc, type);
		}
		
	}
	
	public EditorUser getUser() {
		// No need to sync because final.
		return user;
	}

	public void setDoc(AceDoc doc) {
		setWorkDoc(doc);
		fireChanged(doc, Listener.ChangeType.FROM_OTHER);
	}

	public synchronized AceDoc getDoc() {
		return work;
	}
	
	public synchronized void editorAttached() {
		attached++;
	}

	public synchronized void editorDetached() {
		attached--;
		if (attached==0) {
			mud.removeUserDoc(user);
		}
	}

	


	
	
}
