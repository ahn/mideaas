package org.vaadin.mideaas.frontend;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.vaadin.mideaas.model.User;

import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class HorizontalUserList extends HorizontalLayout {
	
	private final TreeMap<User, UserBox> boxes = new TreeMap<User, UserBox>();
	
	public HorizontalUserList() {
		this(new TreeSet<User>());
	}
	
	public HorizontalUserList(Set<User> loggedInUsers) {
		setSpacing(true);
		setUsers(loggedInUsers);
	}

	public void addUser(User user) {
		if (boxes.containsKey(user)) {
			return;
		}
		boxes.put(user, new UserBox(user));
		draw();
	}
	
	public void removeUser(User user) {
		boxes.remove(user);
		draw();
	}

	private void draw() {
		removeAllComponents();
		for (UserBox ub : boxes.values()) {
			addComponent(ub);
		}
	}

	public void setUsers(Set<User> users) {
		boxes.clear();
		for (User u : users) {
			boxes.put(u, new UserBox(u));
		}
		draw();
	}

}
