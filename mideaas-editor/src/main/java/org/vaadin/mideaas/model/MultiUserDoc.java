package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.Util;

public class MultiUserDoc {

	public enum Type {
		/**
		 * Error-free document.
		 */
		BASE,
		/**
		 * Document containing each users changes.
		 */
		WORK,
		/**
		 * Users own document.
		 */
		USER
	}
	
	public interface BaseChangedListener {
		public void baseChanged(AceDoc doc);
	}
	private CopyOnWriteArrayList<BaseChangedListener> listeners =
			new CopyOnWriteArrayList<BaseChangedListener>();
	
	public interface DifferingUsersChangedListener {
		public void differingUsersChanged(Set<User> users);
	}
	private CopyOnWriteArrayList<DifferingUsersChangedListener> ducListeners =
			new CopyOnWriteArrayList<DifferingUsersChangedListener>();
	
	private static final ExecutorService pool = Executors.newSingleThreadExecutor();
	
	private AceDoc base;
	private final HashMap<String, UserDoc> userDocs = new HashMap<String, UserDoc>();
	private final ErrorChecker checker;

	private Set<User> differing = new HashSet<User>();
	
	private final File saveBaseTo;

	private final ProjectLog log;
	private final String name;
	
	public MultiUserDoc(String name, AceDoc initial, ErrorChecker checker, File saveBaseTo, ProjectLog log) {
		if (checker!=null && !checker.getErrors(initial.getText()).isEmpty()) {
			throw new IllegalArgumentException("The initial value of MUD must be error-free!");
		}
		base = initial;
		
		this.name = name;
		this.checker = checker;
		this.saveBaseTo = saveBaseTo;
		
		this.log = log;
		
		//XXX
		/*
		if (saveBaseTo!=null) {
			writeToDisk(saveBaseTo, initial.getText());
		}
		*/
	}

	// TODO: where to sync?
	public boolean tryToApply(ServerSideDocDiff toBase, User user) {
		
		if (toBase.isIdentity()) {
			if (user!=null) {
				removeDiffering(user);
			}
			return true;
		}
		
		boolean applied = false;
		AceDoc base2;
		synchronized (this) {
			base2 = toBase.applyTo(base);
			if (checker==null || checker.getErrors(base2.getText()).isEmpty()) {
				base = base2;
				applied = true;
			}
		}
		
		if (applied) {
			if (log!=null) {
				log.logUserEdit(name, user, toBase, base2.getText().length());
			}
			if (saveBaseTo!=null) {
				writeToDisk(saveBaseTo, base2.getText());
			}
			fireBaseChanged(base2, user);
		}
		else {
			if (user!=null) {
				addDiffering(user);
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

	private void fireBaseChanged(AceDoc base, User byUser) {
		List<UserDoc> uds = getUserDocs();
		
		HashSet<User> differingUsers = new HashSet<User>();
		
		// TODO: should userdocs add themselves as BaseChangedListeners instead?
		for (UserDoc ud : uds) {
			boolean same = ud.baseChanged(base, byUser);
			if (!same) {
				differingUsers.add(ud.getUser());
			}
		}
		
		for (BaseChangedListener li : listeners) {
			li.baseChanged(base);
		}
		
		setDiffering(differingUsers);
	}
	
	private void setDiffering(Set<User> differing) {
		if (Util.sameSets(differing, this.differing)) {
			return;
		}
		synchronized (this) {
			this.differing = differing;
		}
		fireDifferingChanged();
	}
	
	private void addDiffering(User user) {
		boolean added;
		synchronized (user) {
			added = differing.add(user);
		}
		if (added) {
			fireDifferingChanged();
		}
	}
	
	private void removeDiffering(User user) {
		boolean removed;
		synchronized (this) {
			removed = differing.remove(user);
		}
		if (removed) {
			fireDifferingChanged();
		}
	}

	private void fireDifferingChanged() {
		final HashSet<User> dus = new HashSet<User>(getDifferingUsersNoCopy());
		final List<DifferingUsersChangedListener> listeners = new LinkedList<DifferingUsersChangedListener>(ducListeners);
        pool.submit(new Runnable() {
            @Override
            public void run() {
                for (DifferingUsersChangedListener li : listeners) {
                	li.differingUsersChanged(dus);
                }
            }
        });
	}

	public synchronized Set<User> getDifferingUsers() {
		return new HashSet<User>(differing);
	}
	
	private synchronized Set<User> getDifferingUsersNoCopy() {
		return differing;
	}
	

	public synchronized AceDoc getBase() {
		return base;
	}
	
	/**
	 * If the doc doesn't exist, it is created, initialized with base value.
	 * 
	 * @param user
	 * @return
	 */
	public synchronized SharedDoc getUserDoc(User user) {
		UserDoc ud = userDocs.get(user.getUserId());
		if (ud == null) {
			ud = new UserDoc(user, base);
			ud.setMUD(this);
			userDocs.put(user.getUserId(), ud);
		}
		return ud.getWorking();
	}
	
	public synchronized void removeUserDoc(User user) {
		userDocs.remove(user.getUserId());
	}
	
	private synchronized List<UserDoc> getUserDocs() {
		return new LinkedList<UserDoc>(userDocs.values());
	}

	public void setBaseNoFire(String newText) {
		setBaseNoFire(newText, null);
	}
	
	public AceDoc setBaseNoFire(String newText, User byUser) {
		AceDoc newBase = base.withText(newText);
		synchronized (this) {
			base = newBase;
		}
		return newBase;
	}
	
	public void setBaseAndFire(String newText, User byUser) {
		AceDoc newBase = setBaseNoFire(newText, byUser);
		fireBaseChanged(newBase, byUser);
	}

	public void addBaseChangedListener(BaseChangedListener li) {
		listeners.add(li);
	}
	
	public void removeBaseChangedListener(BaseChangedListener li) {
		listeners.remove(li);
	}
	
	public void addDifferingUsersChangedListener(DifferingUsersChangedListener li) {
		ducListeners.add(li);
	}
	
	public void removeDifferingUsersChangedListener(DifferingUsersChangedListener li) {
		ducListeners.remove(li);
	}
}
