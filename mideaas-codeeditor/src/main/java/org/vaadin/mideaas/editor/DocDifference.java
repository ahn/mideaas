package org.vaadin.mideaas.editor;

import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;

public class DocDifference {
	private final String userId;
	private final AceDoc baseDoc;
	private final AceDoc userDoc;
	private int inserts = -1;
	private int deletes = -1;

	public DocDifference(String userId, AceDoc baseDoc, AceDoc userDoc) {
		this.userId = userId;
		this.baseDoc = baseDoc;
		this.userDoc = userDoc;
	}
	
	public String getUserId() {
		return userId;
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
		if (inserts >= 0) {
			return;
		}
		if (baseDoc==null || userDoc==null) {
			// XXX ???
			inserts = 0;
			deletes = 0;
			return;
		}
		ServerSideDocDiff diff = ServerSideDocDiff.diff(baseDoc, userDoc);
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
		return "DD "+userId+" +" + getInserts() + " -" + getDeletes();
	}
}