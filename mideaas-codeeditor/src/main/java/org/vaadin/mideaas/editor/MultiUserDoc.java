package org.vaadin.mideaas.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.editor.DocDifference;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.SharedDoc;
import org.vaadin.mideaas.editor.SharedDoc.Listener;

public class MultiUserDoc implements Listener {

	public interface DifferingChangedListener {
		public void differingChanged(Map<EditorUser, DocDifference> diffs);
	}
	
	private final CopyOnWriteArrayList<DifferingChangedListener> dcListeners =
			new CopyOnWriteArrayList<DifferingChangedListener>();

	private final SharedDoc base;
	private final Guard upwardsGuard;
	private final Guard downwardsGuard;
	private final Filter filter;
	
	private final HashMap<EditorUser, ChildDoc> childDocs = new HashMap<EditorUser, ChildDoc>();
	
	private final Timer baseChangeTimer = new Timer();
	private boolean fireScheduled = false;

	private final AsyncErrorChecker checker;
	
	private static class ChildDoc {
		final EditorUser user;
		final SharedDoc doc;
		final DocDiffMediator med;
		int numRegistered = 1;
		public ChildDoc(EditorUser user, SharedDoc doc, DocDiffMediator med) {
			this.user = user;
			this.doc = doc;
			this.med = med;
		}
	}
	
	public MultiUserDoc(AceDoc initial, Filter filter, Guard upwardsGuard, Guard downwardsGuard, AsyncErrorChecker checker) {
		this.base = new SharedDoc(initial, checker);
		this.filter = filter;
		this.upwardsGuard = upwardsGuard;
		this.downwardsGuard = downwardsGuard;
		this.checker = checker;
		base.addListener(this);
	}
	
	public SharedDoc getBase() {
		return base;
	}
	
	public String getBaseText() {
		return base.getDoc().getText();
	}
	
	synchronized public SharedDoc getChildDoc(EditorUser user) {
		return childDocs.get(user).doc;
	}
	
	/*
	synchronized public SharedDoc getChildDocCreateIfNeeded(EditorUser user) {
		ChildDoc cd = childDocs.get(user);
		if (cd == null) {
			return createChildDoc(user);
		}
		return cd.doc;
	}
	*/
	
	public SharedDoc registerChildDoc(EditorUser user) {
		boolean changed = false;
		ChildDoc child;
		synchronized (this) {
			child = childDocs.get(user);
			if (child == null) {
				child = createNewChildDoc(user);
				changed = true;
			}
			else {
				child.numRegistered += 1;
			}
			System.out.println(this + "   REGISTER " + user.getId() + " -> " + child.numRegistered+"");
		}
		
		if (changed) {
			fireDifferingChanged(getDifferences());
		}
		
		return child.doc;
	}
	
	private ChildDoc createNewChildDoc(EditorUser user) {
		SharedDoc doc = new SharedDoc(base.getDoc(), checker);
		DocDiffMediator med = new DocDiffMediator(filter, base, doc);
		setGuards(med);
//		setFilter(med);
		ChildDoc cd = new ChildDoc(user, doc, med);
		childDocs.put(user, cd);
		
		doc.addListener(this);
		
		return cd;
	}
	
	public void unregisterChildDoc(EditorUser user) {
		boolean changed = false;
		synchronized (this) {
			ChildDoc child = childDocs.get(user);
			if (child == null) {
				return;
			}
			if (child.numRegistered == 1) {
				removeChildDoc(child);
				changed = true;
			}
			else {
				child.numRegistered -= 1;
			}
			System.out.println(this + " UNREGISTER " + user.getId() + " -> " + child.numRegistered+"");
		}
		
		if (changed) {
			fireDifferingChanged(getDifferences());
		}
	}
	
	private void removeChildDoc(ChildDoc child) {

		childDocs.remove(child.user);
		child.doc.removeListener(this);
		child.med.stop();
	}

	private void setGuards(DocDiffMediator med) {
		if (upwardsGuard != null) {
			med.setUpwardsGuard(upwardsGuard);
		}
		if (downwardsGuard != null) {
			med.setDownwardsGuard(downwardsGuard);
		}
	}
	
//	private void setFilter(DocDiffMediator med) {
//		if (filter != null) {
//			med.setFilter(filter);
//		}
//	}
	
	
	
	public synchronized void addDifferingChangedListener(DifferingChangedListener li) {
		dcListeners.add(li);
	}
	
	public synchronized void removeDifferingChangedListener(DifferingChangedListener li) {
		dcListeners.remove(li);
	}

	@Override
	public void changed() {
		// Delaying a bit. Not acting on each change, only after a while.
		// This is a bit so so...
		synchronized (baseChangeTimer) {
			if (fireScheduled) {
				return;
			}
			baseChangeTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					synchronized (baseChangeTimer) {
//						if (saveBaseTo!=null) {
//							saveBaseToDisk();
//						}
						fireDifferingChanged(getDifferences());
						fireScheduled = false;
					}
				}
			}, 400);
			fireScheduled = true;
		}
	}

	public synchronized Map<EditorUser, DocDifference> getDifferences() {
		HashMap<EditorUser, DocDifference> diffs = new HashMap<EditorUser, DocDifference>();
		for (ChildDoc cd : childDocs.values()) {
			DocDifference dd = new DocDifference(cd.user, base.getDoc(), cd.doc.getDoc());
			//if (dd.isChanged()) {
				diffs.put(cd.user, dd);
			//}
		}
		return diffs;
	}
	
	private void fireDifferingChanged(Map<EditorUser, DocDifference> diffs) {
		for (DifferingChangedListener li : dcListeners) {
			li.differingChanged(diffs);
		}
	}

}
