//package org.vaadin.mideaas.editor;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.vaadin.aceeditor.ServerSideDocDiff;
//import org.vaadin.aceeditor.client.AceDoc;
//import org.vaadin.mideaas.editor.MultiUserDoc.BaseChangedListener;
//
//public class UserDoc implements BaseChangedListener {
//	
//	public interface Listener {
//		public enum ChangeType {
//			FROM_EDITOR,
//			FROM_MUD,
//			FROM_OTHER
//		}
//		void changed(AceDoc doc, ChangeType type);
//	}
//
//	private List<Listener> listeners = new ArrayList<Listener>();
//	
//	private final EditorUser user;
//	private AceDoc shadow;
//	private AceDoc work;
//	private MultiUserDoc mud;
//
//	private SyncMode syncMode = SyncMode.ASAP;
//
//	public UserDoc(EditorUser user, AceDoc initial) {
//		this.user = user;
//		shadow = initial;
//		work = initial;
//	}
//
//	public synchronized void addListener(Listener listener) {
//        listeners.add(listener);
//    }
//
//    public synchronized void removeListener(Listener listener) {
//        listeners.remove(listener);
//    }
//
//    private synchronized List<Listener> getListeners() {
//        return new ArrayList<Listener>(listeners);
//    }
//
//	public synchronized void setMUD(MultiUserDoc mud) {
//		this.mud = mud;
//	}
//	
//	public synchronized SyncMode getSyncMode() {
//		return syncMode;
//	}
//	
//	public synchronized void setSyncMode(SyncMode mode) {
//		syncMode  = mode;
//	}
//	
//	public void editorChanged(ServerSideDocDiff diff) {
//		SyncMode mode = getSyncMode();
//		if (mode==SyncMode.ASAP) {
//			syncDoc(diff);
//		}
//		else if (mode==SyncMode.MANUAL) {
//			//syncDocNoToBase(diff);
//		}
//	}
//	
//	public void syncDoc(ServerSideDocDiff diff) {
//		AceDoc doc;
//		synchronized(mud) {
//			setWorkDoc(diff.applyTo(work));
//			doc = work;
//		}
//		fireChanged(doc, Listener.ChangeType.FROM_EDITOR);
//	}
//
//	private void syncDocNoToBase(AceDoc doc) {
//		AceDoc fireThis = setWorkDocNoToBase(doc);
//		if (fireThis!=null) {
//			fireChanged(fireThis, Listener.ChangeType.FROM_EDITOR);
//		}
//	}
//	
//	private AceDoc setWorkDoc(AceDoc doc) {
//		
//		synchronized (mud) {
//		
//
//		// XXX Debug random timeout
//		try {
//			Thread.sleep((long)(Math.random() * 1000));
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("SWD " + this + "\n  work=" + work.getText() + "\n  shadow=" + shadow.getText() + "\n  doc=" + doc.getText());
//		
//		
//		work = doc;
//		ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, doc);
//		boolean applied = mud.tryToApply(diff, this);
//		
//		// XXX Debug random timeout
//		try {
//			Thread.sleep((long)(Math.random() * 1000));
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		if (!applied) {
//			return work;
//		}
//		
//
//		
//		}
//		
//		
//		return null;
//	}
//	
//	private AceDoc setWorkDocNoToBase(AceDoc doc) {
//		synchronized (mud) {
//			if (doc.equals(work)) {
//				return null;
//			}
//			this.work = doc;
//			ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, doc);
//			mud.dontTryToApply(diff, this);
//			return work;
//		}
//	}
//	
//	
//	public void updateShadow() {
//		shadow = work;
//	}
//	
//	@Override
//	public void baseChanged(AceDoc base, EditorUser byUser) {
//		AceDoc newWork;
//		boolean workChanged;
//		synchronized (mud) {
//
//			// XXX Debug random timeout
//			try {
//				Thread.sleep((long)(Math.random() * 1000));
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			System.out.println("BCH " + this + "\n  work=" + work.getText() + "\n  shadow=" + shadow.getText() + "\n  base=" + base.getText());
//			
//			ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, base);
//			shadow = base;
//			newWork = diff.applyTo(work);
//			workChanged = !work.equals(newWork);
//		}
//		
//		if (workChanged) {
//			fireChanged(newWork, Listener.ChangeType.FROM_MUD);
//			if (syncMode==SyncMode.ASAP) {
//				setWorkDoc(newWork);
//			}
//			else if (syncMode==SyncMode.MANUAL) {
//				setWorkDocNoToBase(newWork);
//			}
//		}
//	}
//
//	private void fireChanged(final AceDoc doc, final Listener.ChangeType type) {
//		System.out.println("fireChanged "+this);
//		for (Listener li : getListeners()) {
//			li.changed(doc, type);
//		}
//		
//	}
//	
//	public EditorUser getUser() {
//		// No need to sync because final.
//		return user;
//	}
//
//	public void setDoc(AceDoc doc) {
//		setWorkDoc(doc);
//		fireChanged(doc, Listener.ChangeType.FROM_OTHER);
//	}
//
//	public AceDoc getDoc() {
//		synchronized (mud) {
//			return work;
//		}
//	}
//		
//	
//}
