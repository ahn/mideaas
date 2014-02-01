package org.vaadin.mideaas.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.vaadin.aceeditor.client.AceDoc;

public class DocManager implements SharedDoc.Listener {
	
	public interface DifferingChangedListener {
		public void differingChanged(Map<EditorUser, DocDifference> diffs);
	}
	
	private final SharedDoc base;
	private final HashMap<EditorUser, JuuserDoc> userDocs
		= new HashMap<EditorUser, JuuserDoc>();
	
	private final CopyOnWriteArrayList<DifferingChangedListener> dcListeners =
			new CopyOnWriteArrayList<DifferingChangedListener>();
	
	// ...
	private final Timer fireTimer = new Timer();
	private boolean fireScheduled = false;
		
	public DocManager(AceDoc initial) {
		base = new SharedDoc(initial);
		base.addListener(this);
	}
	
	public SharedDoc getBase() {
		return base;
	}
	
	public synchronized JuuserDoc getUserDoc(EditorUser user) {
		JuuserDoc ud = userDocs.get(user);
		if (ud==null) {
			ud = createUserDoc(user);
			userDocs.put(user, ud);
		}
		return ud;
	}
	
	public synchronized void removeUserDoc(EditorUser user) {
		JuuserDoc ud = userDocs.remove(user);
		ud.getDoc().removeListener(this);
		if (ud!=null) {
			ud.getMed().detach();
		}
	}
	
	public synchronized Map<EditorUser, DocDifference> getDifferences() {
		HashMap<EditorUser, DocDifference> diffs = new HashMap<EditorUser, DocDifference>();
		for (JuuserDoc ud : userDocs.values()) {
			DocDifference dd = ud.getDiff();
			if (dd.isChanged()) {
				diffs.put(ud.getUser(), dd);
			}
		}
		return diffs;
	}
	
	private JuuserDoc createUserDoc(EditorUser user) {
		SharedDoc doc = new SharedDoc(getBase().getDoc());
		DocDiffMediator med = new DocDiffMediator(base, doc);
		med.setUpwardsGuard(new JavaSyntaxGuard());
		JuuserDoc ud = new JuuserDoc(user, doc, med, base);
		doc.addListener(this);
		return ud;
	}

	@Override
	public void changed() {
		// Delaying firing. Not sending each change, only after a while.
		// This is a bit so so...
		synchronized (fireTimer) {
			if (fireScheduled) {
				return;
			}
			fireTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					synchronized (fireTimer) {
						fireDifferingChanged(getDifferences());
						fireScheduled = false;
					}
				}
			}, 400);
			fireScheduled = true;
		}
	}
	
	public synchronized void addDifferingChangedListener(DifferingChangedListener li) {
		dcListeners.add(li);
	}
	
	public synchronized void removeDifferingChangedListener(DifferingChangedListener li) {
		dcListeners.remove(li);
	}
	
	private void fireDifferingChanged(Map<EditorUser, DocDifference> diffs) {
		for (DifferingChangedListener li : dcListeners) {
			li.differingChanged(diffs);
		}
	}

	public String getBaseText() {
		return base.getDoc().getText();
	}

	public void setBaseNoFire(String xml) {
		base.setDoc(new AceDoc(xml));
	}
	
}
