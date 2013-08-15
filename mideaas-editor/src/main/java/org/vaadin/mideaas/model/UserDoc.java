package org.vaadin.mideaas.model;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;

public class UserDoc {
	
	private final User user;
	private AceDoc shadow;
	private final SharedDoc work;
	private MultiUserDoc mud;
	
	private SharedDoc.Listener workListener = new SharedDoc.Listener() {
		@Override
		public void changed(SharedDocRevision rev) {
			workChanged(rev);
		}
	};
	
	public UserDoc(User user, AceDoc initial) {
		this.user = user;
		shadow = initial;
		work = new SharedDoc(shadow);
		
	}
	
	public synchronized void setMUD(MultiUserDoc mud) {
		this.mud = mud;
		work.register(workListener);
	}
	
	private synchronized /* <-- TODO too much? */ void workChanged(SharedDocRevision rev) {
		AceDoc shadow = this.shadow;
		ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, rev.getDoc());
		
		boolean applied = mud.tryToApply(diff, user);
		if (applied) {
			this.shadow = rev.getDoc();
		}
	}
	
	public SharedDoc getWorking() {
		// No need to sync because final.
		return work;
	}
	
	/**
	 * 
	 * @param base
	 * @return true iff in the end base==userdoc
	 */
	public synchronized boolean baseChanged(AceDoc base, User byUser) {
		if (!user.equals(byUser)) {
			ServerSideDocDiff diff = ServerSideDocDiff.diff(shadow, base);
			shadow = base;
			work.applyDiff(diff);
		}
		return work.getDoc().equals(base);
		
		//System.out.println("WRITE USERDOC TO DISK "+user.getName()+"\n"+base.getText());
	}
	
	public User getUser() {
		// No need to sync because final.
		return user;
	}
	
}
