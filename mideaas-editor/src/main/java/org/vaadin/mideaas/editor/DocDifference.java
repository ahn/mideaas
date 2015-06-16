package org.vaadin.mideaas.editor;

import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;

public class DocDifference {
	private final EditorUser user;
	private final AceDoc baseDoc;
	private final AceDoc userDoc;
	private ServerSideDocDiff diff;
	private int inserts = -1;
	private int deletes = -1;

	public DocDifference(EditorUser user, AceDoc baseDoc, AceDoc userDoc) {
		this.user = user;
		this.baseDoc = baseDoc;
		this.userDoc = userDoc;
	}
	
	public EditorUser getUser() {
		return user;
	}
	
	public ServerSideDocDiff getDiff() {
		ensureCalced();
		return diff;
	}

	public int getInserts() {
		ensureCalced();
		return inserts;
	}

	public int getDeletes() {
		ensureCalced();
		return deletes;
	}
	
	public boolean isChanged() {
		ensureCalced();
		return inserts > 0 || deletes > 0;
	}

	private synchronized void ensureCalced() {
		if (diff != null) {
			return;
		}
		if (baseDoc==null || userDoc==null) {
			inserts = 0;
			deletes = 0;
			return;
		}
		diff = ServerSideDocDiff.diff(baseDoc, userDoc);
		inserts = 0;
		deletes = 0;
		for (Patch p : diff.getPatches()) {
			for( Diff d : p.diffs) {
				if (d.operation==Operation.INSERT) {
					inserts += d.text.length();
				}
				else if (d.operation==Operation.DELETE) {
					deletes += d.text.length();
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "DD "+user.getName()+" +" + getInserts() + " -" + getDeletes();
	}

	// TODO
//	@Override
//	public boolean equals(Object other) {
//		if (other instanceof DocDifference) {
//			DocDifference odd = (DocDifference)other;
//			
//			
//		}
//		return false;
//	}

	public String buttonText() {
		int inserts = getInserts();
		int deletes = getDeletes();
		if (inserts>0 || deletes>0) {
			return "+"+inserts+"\n-"+deletes;
		}
		else {
			return null;
		}
	}
}