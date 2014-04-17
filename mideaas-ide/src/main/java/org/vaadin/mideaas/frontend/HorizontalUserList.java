package org.vaadin.mideaas.frontend;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.vaadin.mideaas.model.User;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
//public class HorizontalUserList extends HorizontalLayout {
public class HorizontalUserList extends Panel {
	
	private final TreeMap<User, UserBox> boxes = new TreeMap<User, UserBox>();
	HorizontalLayout panelLayout = null;
	
	public HorizontalUserList() {
		this(new TreeSet<User>());
		//setWidth("100%");
	}
	
	public HorizontalUserList(Set<User> loggedInUsers) {
		//setSpacing(true);
		panelLayout = new HorizontalLayout();
		//panelLayout.setMargin(true);
		panelLayout.setStyleName("userlist-layout");
		setContent(panelLayout);
		//panelLayout.setWidth("100%");
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
		/*
		removeAllComponents();
		for (UserBox ub : boxes.values()) {
			addComponent(ub);
		}
		*/
		panelLayout.removeAllComponents();
		for (UserBox ub : boxes.values()) {
			panelLayout.addComponent(ub);
			//panelLayout.setComponentAlignment(ub, Alignment.MIDDLE_CENTER);
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
