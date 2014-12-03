package org.vaadin.mideaas.ide;

import org.vaadin.chatbox.ChatBox;
import org.vaadin.chatbox.SharedChat;
import org.vaadin.chatbox.client.ChatUser;
import org.vaadin.mideaas.editor.EditorUser;

import com.vaadin.annotations.StyleSheet;

@SuppressWarnings("serial")
@StyleSheet("idechatbox.css")
public class IdeChatBox extends ChatBox {

	public IdeChatBox(SharedChat chat, EditorUser user) {
		super(chat);
		setUser(new ChatUser(user.getId(), user.getName(), "chatbox-collab-" + user.getStyleIndex()));
		setShowSendButton(false);
	}

}