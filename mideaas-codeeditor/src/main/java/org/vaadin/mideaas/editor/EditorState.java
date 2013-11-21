package org.vaadin.mideaas.editor;


public class EditorState {
	
	public enum DocType {
		BASE,
		MINE,
		OTHERS
	}
	
	public final DocType type;
	public final String userId;
	public EditorState(DocType type, String userId) {
		this.type = type;
		this.userId = userId;
	}
	@Override public boolean equals(Object o) {
		if (o instanceof EditorState) {
			EditorState oe = (EditorState)o;
			return oe.type==type && (userId==null ? oe.userId==null : userId.equals(oe.userId));
		}
		return false;
	}
	@Override public int hashCode() {
		return type.hashCode() * (userId==null ? 1 : userId.hashCode()); // ?
	}
	@Override public String toString() {
		return userId==null ? type.toString() : type.toString()+":"+userId;
	}
}