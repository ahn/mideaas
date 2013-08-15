package org.vaadin.mideaas.model;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;

public class SharedDocRevision {
	private final long revision;
	private final AceDoc doc;
	private final ServerSideDocDiff diff;
	
	public SharedDocRevision(long revision, AceDoc doc, ServerSideDocDiff diff) {
		this.revision = revision;
		this.doc = doc;
		this.diff = diff;
	}

	public long getRevision() {
		return revision;
	}

	public AceDoc getDoc() {
		return doc;
	}

	public ServerSideDocDiff getDiff() {
		return diff;
	}
	
	

}
