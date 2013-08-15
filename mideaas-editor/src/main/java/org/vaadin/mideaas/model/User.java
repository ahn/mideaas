package org.vaadin.mideaas.model;

import java.util.HashMap;

public class User implements Comparable<User> {

	protected static HashMap<String, User> users = new HashMap<String, User>();

	private static Integer latestUserId = 0;

	protected final String userId;
	protected final String name;

	private String oauthToken = null;
	
	synchronized protected void putUser(User u) {
		users.put(u.getUserId(), u);
	}

	synchronized public static User getUser(String userId) {
		return users.get(userId);
	}

	synchronized public static User newUser(String name) {
		User user = new User(newUserId(), name);
		return user;
	}
	
	synchronized protected static String newUserId() {
		return "" + (++latestUserId);
	}

	protected User(String userId, String name) {
		this.userId = userId;
		this.name = name;
		putUser(this);
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			return ((User) obj).userId.equals(userId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return userId.hashCode();
	}

	public String getOAuthToken() {
		return oauthToken;
	}

	public void setOAuthToken(String value1) {
		oauthToken = value1;
	}

	@Override
	public String toString() {
		return getUserId() + ":" + getName();
	}

	@Override
	public int compareTo(User o) {
		int c = getName().compareTo(o.getName());
		// For unambiguous comparison (total order),
		// compare the id's of same-name Users.
		if (c == 0) {
			return getUserId().compareTo(o.getUserId());
		}
		return c;
	}
}
