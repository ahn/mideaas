package org.vaadin.mideaas.ide.model;

import java.util.HashMap;

import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.ide.social.OAuthService;
import org.vaadin.mideaas.ide.social.UserProfile;
import org.vaadin.mideaas.ide.social.OAuthService.Service;

public class User implements Comparable<User> {

	protected static HashMap<String, User> users = new HashMap<String, User>();

	private static Integer latestUserId = 0;

	private final String userId;
	
	private final EditorUser editorUser;
	
	private UserProfile activeProfile;

	private HashMap<OAuthService.Service, UserProfile> profiles =
			new HashMap<OAuthService.Service, UserProfile>();
	
	synchronized protected void putUser(User u) {
		users.put(u.getUserId(), u);
	}

	synchronized static public User getUser(String userId) {
		return users.get(userId);
	}

	synchronized public static User newUser(String name) {
		User user = new User(newUserId(), name);
		return user;
	}
	
	synchronized public static User newUser(UserProfile profile) {
		User user = new User(newUserId(), profile);
		return user;
	}
	
	synchronized protected static String newUserId() {
		return "" + (++latestUserId);
	}
	
	protected User(String userId, String name) {
		this(userId, name, null);
	}
	
	protected User(String userId, String name, String email) {
		this(userId, createDefaultProfile(userId, name, email));
	}

	protected User(String userId, UserProfile profile) {
		this.userId = userId;
		editorUser = new EditorUser(userId, profile.getName(), profile.getEmail());
		// TODO: editorUser name won't change even if user name changes...
		
		profiles.put(profile.getService(), profile);
		activeProfile = profile;
		putUser(this);
	}
	
	private static UserProfile createDefaultProfile(String id, String name, String email) {
		return new UserProfile(Service.DEFAULT, null, id, name, email, null);
	}

	public String getUserId() {
		return userId;
	}

	synchronized public String getName() {
		return activeProfile.getName();
	}
	
	synchronized public String getEmail() {
		return activeProfile.getEmail();
	}
	
	synchronized public String getImgUrl() {
		return activeProfile.getImgUrl();
	}
	
	synchronized public void addProfile(UserProfile profile) {
		profiles.put(profile.getService(), profile);
	}
	
	synchronized public UserProfile getProfile(Service service) {
		return profiles.get(service);
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

	public EditorUser getEditorUser() {
		return editorUser;
	}
	
	
}
