package org.vaadin.mideaas.editor.oauth;


import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

public class GitHubService extends OAuthService {

	public GitHubService(String apiKey, String apiSecret, UserToken token) {
		super(Service.GITHUB, apiKey, apiSecret, token);
	}

	private GitHub gh;
	
	@Override
	protected void connect() {
		try {
			gh = GitHub.connectUsingOAuth(getUserToken().getToken());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected UserProfile fetchUserProfile() {
		if (gh==null) {
			return null;
		}
		
		try {
			GHMyself me = gh.getMyself();
			String id = me.getLogin();
			String name = me.getName();
			String email = me.getEmail();
			String imgUrl = me.getAvatarUrl();
			return new UserProfile(Service.GITHUB, getUserToken(), id, name, email, imgUrl);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	   
	}
	
	
	
	public List<GHGist> getGists(String login) {
		try {
			GHUser user = gh.getUser(login);
			return user.listGists().asList();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String createRepository(String projectName) throws IOException {
		connect();
		String name = gh.getMyself().getLogin()+"/"+projectName;
		try {
			GHRepository r = gh.getRepository(name);
			return getHttpsUrl(r);
		} catch (IOException e) {
			// No such repo found. No prob, we create it.
		}
		String descr = projectName + " created by MIDEaaS";
		GHRepository r = gh.createRepository(projectName, descr, null, true);
		return getHttpsUrl(r);
	}

	private String getHttpsUrl(GHRepository r) {
		String url = r.getUrl();
		return url.replace("http://", "https://").replaceFirst("/?$", ".git");
	}

	public GHGist getGist(String id) {
		try {
			return gh.getGist(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
