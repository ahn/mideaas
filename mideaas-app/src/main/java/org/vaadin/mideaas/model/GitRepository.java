package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.vaadin.ui.Notification;

/**
 * The Class GitRepository.
 */
public class GitRepository {
	
	/** The git. */
	private final Git git;
    /** The Constant gitUrlPattern. */
    private static final Pattern gitUrlPattern = Pattern.compile(".*/([^/.]+).git");
	
	/**
	 * Inits new gitrepository to the directory given
	 *
	 * @param dir of new repository
	 * @return the git repository
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static GitRepository initAt(File dir) throws IOException {
		Repository newRepo = new FileRepository(new File(dir, ".git"));
		newRepo.create();
		return new GitRepository(new Git(newRepo));
	}

	/**
	 * Inits new gitrepository from directory.
	 *
	 * @param dir the dir
	 * @return the git repository
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static GitRepository fromExistingGitDir(File dir) throws IOException {
		//tries to find .git file from directory
		File gitDir = new File(dir, ".git");
		Git repo = Git.open(gitDir);
		return new GitRepository(repo);
	}
	

	public static GitRepository cloneFrom(String gitUrl, File destDir) {
		
		try {
			deleteContent(destDir);
			CloneCommand command = Git.cloneRepository();
			command.setURI(gitUrl);
			command.setDirectory(destDir);
			return new GitRepository(command.call());
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; // XXX?
	}
	
	
	/**
	 * Delete content.
	 *
	 * @param dir the dir
	 */
	private static void deleteContent(File dir) {
		File[] files;
		try {
			files = dir.listFiles();
			while(files.length>0){
				FileUtils.deleteDirectory(files[0]);
				files = dir.listFiles();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Default project name from git url.
	 *
	 * @param gitUrl the git url
	 * @return the string
	 */
	public static String defaultProjectNameFromGitUrl(String gitUrl) {
    	Matcher matcher = gitUrlPattern.matcher(gitUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a proper git url(?): "+gitUrl);
        }
        return matcher.group(1);
    }
	
	/**
	 * Instantiates a new git repository.
	 *
	 * @param git the git
	 */
	private GitRepository(Git git) {
		this.git = git;
	}

	/**
	 * Adds all the source files to git.
	 *
	 * @throws GitAPIException the git api exception
	 */
	public void addSourceFilesToGit() throws GitAPIException {
		git.add().addFilepattern("src/main/java").call();
		git.add().addFilepattern("src/main/resources").call();
		git.add().addFilepattern("src/main/webapp/WEB-INF").call();
		git.add().addFilepattern("src/main/webapp/META-INF").call();
		git.add().addFilepattern("pom.xml").call();
	}

	/**
	 * Commit all.
	 *
	 * @param msg the msg
	 * @throws GitAPIException the git api exception
	 */
	public void commitAll(String msg) throws GitAPIException {
		CommitCommand command = git.commit().setMessage(msg).setAll(true);
		RevCommit result = command.call();
		String message=result.getFullMessage();
		if (message.length()>0){
			Notification.show("Commit ok, message: " + message);			
		}else{
			Notification.show("Commit ok, no commit message :( ?");
		}	
	}
	
	public void pushAll(String userName, String password, String remoteName) throws GitAPIException { 
        // credentials
        CredentialsProvider credentials = null;
        credentials = new UsernamePasswordCredentialsProvider(userName, password);
        
        try{
	        PushCommand command = git.push().setRemote(remoteName);
			command.setCredentialsProvider(credentials);
			Iterable<PushResult> results = command.call();
			int updates = 0;
			for (PushResult result:results){
				updates += result.getRemoteUpdates().size();
			}
			if (updates==0){
				Notification.show("No updates pushed. Something maybe failed?",  Notification.Type.WARNING_MESSAGE);
			}else if (updates==1){
				Notification.show("Update pushed.");
			}else{
				Notification.show(updates + " updates pushed.");
			}
		}catch(JGitInternalException e){
        	Notification.show("Push failed. Did you remember to commit first? " + e.getMessage(),  Notification.Type.ERROR_MESSAGE);
        }catch(InvalidRemoteException e){
        	Notification.show("Push failed: " + e.getMessage(),  Notification.Type.ERROR_MESSAGE);
        }catch(TransportException e){
        	Notification.show("Push failed: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
        } 
	}


	public String getRemote(String name){
		StoredConfig config = this.git.getRepository().getConfig();
		String url = config.getString("remote", name, "url");
		return url;
	}
	
	public boolean hasCommit(){
		return !git.getRepository().getAllRefs().isEmpty();
	}

	
	public void addRemote(String name, String url) {
		Repository repo = this.git.getRepository();
		StoredConfig config = repo.getConfig();
		config.setString("remote", name, "url", url);
		try {
			config.save();
        	Notification.show("Remote "+name+" set to: " + url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
        	Notification.show("Set origin failed.");
			e.printStackTrace();
		}
	}
}
