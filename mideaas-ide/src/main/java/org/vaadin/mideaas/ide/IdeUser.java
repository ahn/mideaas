package org.vaadin.mideaas.ide;

import java.util.UUID;

import org.vaadin.mideaas.editor.EditorUser;

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

	public static String randomUserId() {
		return UUID.randomUUID().toString();
	}
	


	
}
