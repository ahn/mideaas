package org.vaadin.mideaas.ide;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.mideaas.editor.MultiUserDoc;

public class IdeDoc {
	private final MultiUserDoc doc;
	private final SharedChat chat;
	private AceMode mode;
	private IdeDoc(MultiUserDoc doc, AceMode mode, SharedChat chat) {
		this.doc = doc;
		this.mode = mode;
		this.chat = chat;
	}
	public IdeDoc(MultiUserDoc doc, AceMode mode) {
		this(doc, mode, new SharedChat());
	}
	public MultiUserDoc getDoc() {
		return doc;
	}
	public SharedChat getChat() {
		return chat;
	}
	public AceMode getAceMode() {
		return mode;
	}
	public String getText() {
		return doc.getBaseText();
	}
	
}
