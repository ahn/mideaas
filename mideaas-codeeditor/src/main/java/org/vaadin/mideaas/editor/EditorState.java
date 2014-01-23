package org.vaadin.mideaas.editor;


/** 
 * The state of MultiUserEditor.
 * 
 * The editor can show my code, other peoples code, or the error-free base code.
 *
 */
public class EditorState {
	
	public enum DocType {
		BASE,
		MINE,
		OTHERS
	}
	
	public final DocType type;
	public final DocDifference diff;
	public EditorState(DocType type, DocDifference diff) {
		this.type = type;
		this.diff = diff;
	}
	@Override public boolean equals(Object o) {
		if (o instanceof EditorState) {
			EditorState oe = (EditorState)o;
			return oe.type==type && (diff==null ? oe.diff==null : diff.getUser().equals(oe.diff.getUser()));
		}
		return false;
	}
	@Override public int hashCode() {
		return type.hashCode() * (diff==null ? 1 : diff.getUser().hashCode()); // ?
	}
	@Override public String toString() {
		if (diff==null || !diff.isChanged()) {
			return "Shared";
		}
		else {
			return diff.getUser().getName() + " (+"+diff.getInserts()+" -"+diff.getDeletes()+")";
		}
	}
}