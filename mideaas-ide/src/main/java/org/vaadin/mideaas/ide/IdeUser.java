package org.vaadin.mideaas.ide;

import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.ide.oauth.UserToken;

public class IdeUser {
	private final EditorUser editorUser;

	public IdeUser(String id, String name, String email) {
		editorUser = new EditorUser(id, name, email);
	}
	
	public String getId() {
		return editorUser.getId();
	}
	
	public String getName() {
		return editorUser.getName();
	}
	
	public String getEmail() {
		return editorUser.getEmail();
	}
	
	public EditorUser getEditorUser() {
		return editorUser;
	}
	


	
}
