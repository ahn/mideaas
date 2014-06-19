package org.vaadin.mideaas.editor;

public class EditorUser {
	private final String id;
	private final String name;
	public EditorUser(String id, String name) {
		this.id = id;
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 */
	public long getStyleIndex() {
		return Math.abs(id.hashCode()) % 6;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof EditorUser) {
			return ((EditorUser)o).id.equals(id);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
