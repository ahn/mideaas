package org.vaadin.mideaas.editor;

public class EditorUser implements Comparable<EditorUser> {
	private final String id;
	private final String name;
	private final String email;
	
	public EditorUser(String id, String name) {
		this(id, name, null);
	}

	public EditorUser(String id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
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
	
	@Override
	public int compareTo(EditorUser other) {
		return id.compareTo(other.id);
	}
	
	public String getGravatarUrl(int size) {
		return "//www.gravatar.com/avatar/" + MD5Util.md5Hex(email!=null?email:(name!=null?name:id)) + "?d=monsterid&s=" + size;
	}

	@Override
	public String toString() {
		return "{id=\""+id+"\", name=\""+name+"\", email=\""+email+"\"}";
	}
	
	
}
