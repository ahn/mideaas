package org.vaadin.mideaas.editor;

import org.vaadin.chatbox.ChatBox;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.chatbox.client.ChatUser;

import com.vaadin.annotations.StyleSheet;

@SuppressWarnings("serial")
@StyleSheet("idechatbox.css")
public class IdeChatBox extends ChatBox {
	
	
	private final SharedChat chat;
	private final EditorUser user;

	public IdeChatBox(SharedChat chat, EditorUser user) {
		super(chat);
		this.chat = chat;
		this.user = user;
		setUser(new ChatUser(user.getId(), user.getName(), "chatbox-collab-"+user.getStyleIndex()));
		setShowSendButton(false);
	}
	
	@Override
	public void attach() {
		super.attach();
		//chat.addLine(user.getName() + " opened the doc");
	}
	
	@Override
	public void detach() {
		//chat.addLine(user.getName() + " closed the doc");
		super.detach();
	}
}
