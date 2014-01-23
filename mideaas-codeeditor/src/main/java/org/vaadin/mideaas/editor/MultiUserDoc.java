package org.vaadin.mideaas.editor;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.FileUtils;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;

public class MultiUserDoc {
	
	public interface BaseChangedListener {
		public void baseChanged(AceDoc doc, EditorUser byUser);
	}
	
	// Not sure if it's necessary to use weak references here,
	// since we're removing the listeners in removeUserDoc.
	// But not sure if removeUserDoc is called in all cases...
	// If not, using weak refs allows the listener object to be garbage collected.
	private CopyOnWriteArrayList<WeakReference<BaseChangedListener>> listeners =
			new CopyOnWriteArrayList<WeakReference<BaseChangedListener>>();
	
	public interface DifferingChangedListener {
		public void differencesChanged(Set<DocDifference> diffs);
	}
	private CopyOnWriteArrayList<DifferingChangedListener> ducListeners =
			new CopyOnWriteArrayList<DifferingChangedListener>();
		
	private AceDoc base;
	private final HashMap<EditorUser, UserDoc> userDocs = new HashMap<EditorUser, UserDoc>();
	private final ErrorChecker checker;

	//private Set<String> differing = new HashSet<String>();
	private HashMap<EditorUser,DocDifference> differences = new HashMap<EditorUser,DocDifference>();
	
	private final File saveBaseTo;
	
	public MultiUserDoc(String name, AceDoc initial, ErrorChecker checker, File saveBaseTo) {
		if (checker!=null && !checker.getErrors(initial.getText()).isEmpty()) {
			throw new IllegalArgumentException("The initial value of MUD must be error-free!");
		}
		base = initial;
		
		this.checker = checker;
		this.saveBaseTo = saveBaseTo;
	}
	
	public void dontTryToApply(ServerSideDocDiff toBase, UserDoc byUserDoc) {
		EditorUser user = byUserDoc==null ? null : byUserDoc.getUser();
		if (toBase.isIdentity()) {
			if (user!=null) {
				removeDiffering(user);
			}
			return;
		}
		
		if (user!=null) {
			addDiffering(byUserDoc);
		}
		
	}

	/**
	 * Tries to apply the given diff to base.
	 * 
	 * The base candidate is checked for errors. If it's error-free,
	 * that's our new base.
	 * 
	 * @return base changed
	 */
	public boolean tryToApply(ServerSideDocDiff toBase, UserDoc byUserDoc) {
		EditorUser user = byUserDoc==null ? null : byUserDoc.getUser();
		if (toBase.isIdentity()) {
			if (user!=null) {
				removeDiffering(user);
			}
			return false;
		}
		
		boolean applied = false;
		AceDoc baseCandidate;
		synchronized (this) {
			baseCandidate = toBase.applyTo(base);
			if (checker==null || checker.getErrors(baseCandidate.getText()).isEmpty()) {
				base = baseCandidate;
				applied = true;
				if (byUserDoc!=null) {
					byUserDoc.updateShadow();
				}
			}
		}
		
		if (applied) {
			if (saveBaseTo!=null) {
				writeToDisk(saveBaseTo, baseCandidate.getText());
			}
			fireBaseChanged(baseCandidate, user);
			if (user!=null) {
				removeDiffering(user);
			}
		}
		else {
			if (user!=null) {
				addDiffering(byUserDoc);
			}
		}
		
		return applied;
	}
	
	private static void writeToDisk(File f, String s) {
		try {
			FileUtils.write(f, s);
		} catch (IOException e) {
			System.err.println("WARNING: could not write to "+f);
			e.printStackTrace();
		}
	}
	
	private void fireBaseChanged(final AceDoc base, EditorUser byUser) {
		boolean cleanup = false;
		for (WeakReference<BaseChangedListener> ref : listeners) {
			BaseChangedListener li = ref.get();
			if (li!=null) {
				li.baseChanged(base, byUser);
			}
			else {
				cleanup = true;
			}
		}
		
		if (cleanup) {
			cleanupBaseChangedListeners();
		}
	}


	private void addDiffering(UserDoc userDoc) {
		synchronized (this) {
			EditorUser user = userDoc.getUser();
			differences.put(user, new DocDifference(user, getBase(), userDoc.getDoc()));
		}
		fireDifferingChanged();
	}
	
	public void removeDiffering(EditorUser user) {
		DocDifference removed;
		synchronized (this) {
			removed = differences.remove(user);
		}
		if (removed!=null) {
			fireDifferingChanged();
		}
	}
	
	synchronized public Set<DocDifference> getDifferences() {
		return new HashSet<DocDifference>(differences.values());
	}
	
	private void fireDifferingChanged() {
		Set<DocDifference> diffs = getDifferences();
		final List<DifferingChangedListener> listeners
				= new LinkedList<DifferingChangedListener>(ducListeners);
		for (DifferingChangedListener li : listeners) {
        	li.differencesChanged(diffs);
        }
	}

//	private void fireDifferingChanged() {
//		final HashSet<String> dus = new HashSet<String>(getDifferingUsersNoCopy());
//		final List<DifferingUsersChangedListener> listeners
//				= new LinkedList<DifferingUsersChangedListener>(ducListeners);
//
//        for (DifferingUsersChangedListener li : listeners) {
//        	li.differingUsersChanged(dus);
//        }

//	}

//	public synchronized Set<String> getDifferingUsers() {
//		return new HashSet<String>(differing);
//	}
//	
//	private synchronized Set<String> getDifferingUsersNoCopy() {
//		return differing;
//	}
	

	public synchronized AceDoc getBase() {
		return base;
	}
	
	/**
	 * If the doc exists, it's returned. If not, initialized with base value.
	 * 
	 * @param user
	 * @return
	 */
	public synchronized UserDoc createUserDoc(EditorUser user) {
		UserDoc ud = userDocs.get(user);
		if (ud == null) {
			ud = new UserDoc(user, base);
			ud.setMUD(this);
			addBaseChangedListenerWeak(ud);
			userDocs.put(user, ud);
		}
		return ud;
	}
	

	public synchronized UserDoc getUserDoc(EditorUser user) {
		return userDocs.get(user);
	}
	
	public synchronized void removeUserDoc(EditorUser user) {
		UserDoc ud = userDocs.remove(user);
		if (ud!=null) {
			removeBaseChangedListenerWeak(ud);
			removeDiffering(user);
		}
	}

	public void setBaseNoFire(String newText) {
		setBaseNoFire(newText, null);
	}
	
	public AceDoc setBaseNoFire(String newText, EditorUser byUser) {
		AceDoc newBase = base.withText(newText);
		synchronized (this) {
			base = newBase;
		}
		return newBase;
	}
	
	public void setBaseAndFire(String newText, EditorUser byUser) {
		AceDoc newBase = setBaseNoFire(newText, byUser);
		fireBaseChanged(newBase, byUser);
	}

	public void addBaseChangedListenerWeak(BaseChangedListener li) {
		listeners.add(new WeakReference<BaseChangedListener>(li));
	}
	
	public void removeBaseChangedListenerWeak(BaseChangedListener li) {
		for (WeakReference<BaseChangedListener> ref : listeners) {
			BaseChangedListener rl = ref.get();
			if (li==rl) {
				listeners.remove(ref);
				return;
			}
		}
	}
	
	private void cleanupBaseChangedListeners() {
		for (WeakReference<BaseChangedListener> ref : listeners) {
			BaseChangedListener rl = ref.get();
			if (rl==null) {
				listeners.remove(ref);
				// recursive call, TODO not the nicest way to go but works i guess
				cleanupBaseChangedListeners();
				return;
			}
		}
	}
	
	public void addDifferingChangedListener(DifferingChangedListener li) {
		ducListeners.add(li);
	}
	
	public void removeDifferingChangedListener(DifferingChangedListener li) {
		ducListeners.remove(li);
	}

	

}
