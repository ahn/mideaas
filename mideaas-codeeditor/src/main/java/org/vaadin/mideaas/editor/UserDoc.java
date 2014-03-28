package org.vaadin.mideaas.editor;

public class UserDoc {
	private final EditorUser user;
	private final SharedDoc doc;
	private final DocDiffMediator med;
	private final SharedDoc base;
	public UserDoc(EditorUser user, SharedDoc doc, DocDiffMediator med, SharedDoc base) {
		this.user = user;
		this.doc = doc;
		this.med = med;
		this.base = base;
	}
	public EditorUser getUser() {
		return user;
	}
	public SharedDoc getDoc() {
		return doc;
	}
	public DocDiffMediator getMed() {
		return med;
	}
	
	public DocDifference getDiff() {
		return new DocDifference(user, base.getDoc(), doc.getDoc());
	}
}