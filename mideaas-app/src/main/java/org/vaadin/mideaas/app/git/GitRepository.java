package org.vaadin.mideaas.app.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;

import com.vaadin.ui.Notification;


public class GitRepository {
	

	private final Git git;
    private static final Pattern gitUrlPattern = Pattern.compile(".*/([^/.]+).git");
	
	/**
	 * Inits new gitrepository to the directory given
	 *
	 * @param dir of new repository
	 * @return the git repository
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static GitRepository initAt(File dir) throws IOException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository newRepo = builder.setGitDir(dir)
		  .readEnvironment() // scan environment GIT_* variables
		  .findGitDir() // scan up the file system tree
		  .build();
		return new GitRepository(new Git(newRepo));
	}

	public static GitRepository fromExistingGitDir(File dir) throws IOException {
		//tries to find .git file from directory
		File gitDir = new File(dir, ".git");
		Git repo = Git.open(gitDir);
		return new GitRepository(repo);
	}
	
	// TODO: do we want to expose this...
	public Git getJGit() {
		return git;
	}
	
	public String diffToHead(String path) throws GitAPIException, IOException {
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DiffFormatter formatter = new DiffFormatter(stream);
	    formatter.setRepository( git.getRepository() );
	    ObjectId commitId = git.getRepository().resolve("HEAD");
	    AbstractTreeIterator commitTreeIterator = prepareTreeParser( git.getRepository(),  commitId);
	    FileTreeIterator workTreeIterator = new FileTreeIterator( git.getRepository() );
	    List<DiffEntry> diffs = formatter.scan( commitTreeIterator, workTreeIterator );
	    
	    //List<DiffEntry> diffs = git.diff().setPathFilter(PathFilter.create(path)).call();

	    for( DiffEntry entry : diffs ) {
	      System.out.println( "Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId() );
	      formatter.format( entry );
	    }
	    
	    return stream.toString("utf-8");
	    
	    //formatter.close();
	}
	
	private static AbstractTreeIterator prepareTreeParser(
			Repository repository, ObjectId objectId) throws IOException,
			MissingObjectException, IncorrectObjectTypeException {
		// from the commit we can build the tree which allows us to construct
		// the TreeParser
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = walk.parseCommit(objectId);
		RevTree tree = walk.parseTree(commit.getTree().getId());

		CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		ObjectReader oldReader = repository.newObjectReader();
		try {
			oldTreeParser.reset(oldReader, tree.getId());
		} finally {
			//oldReader.close();
			//walk.close();
		}

		walk.dispose();

		return oldTreeParser;
	}
	

	public static GitRepository cloneFrom(String gitUrl, File destDir, String login, String password) throws GitAPIException {
		CloneCommand command = Git.cloneRepository();
		command.setURI(gitUrl);
		command.setDirectory(destDir);
		UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(login, password); 
		command.setCredentialsProvider(auth);
		return new GitRepository(command.call());
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

	private GitRepository(Git git) {
		this.git = git;
	}
	
	public Status status() throws GitAPIException {
		Status status = git.status().call();
		return status;
	}
	
	public String getBranch() throws IOException {
		return git.getRepository().getBranch();
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
	
	public Set<String> getRemotes() {
		return git.getRepository().getRemoteNames();
	}

	public void commit(Collection<String> files, String message) throws NoFilepatternException, GitAPIException {
		AddCommand add = git.add();
		for (String f : files) {
			add.addFilepattern(f);
		}
		add.call();
		
		git.commit().setMessage(message).call();
	}

	public Iterable<RevCommit> log() throws NoHeadException, GitAPIException {
		return git.log().call();
	}

	public PullResult pullFrom(String remote) throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {
		return git.pull().setRemote(remote).call();
	}

	public String checkoutNewBranch(String branchName) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, GitAPIException {
		return git.checkout().setCreateBranch(true).setName(branchName).call().getName();
	}

	public Iterable<PushResult> pushTo(String remote, String username, String password) throws InvalidRemoteException, TransportException, GitAPIException {
		CredentialsProvider creds = new UsernamePasswordCredentialsProvider(username, password);
		return git.push().setCredentialsProvider(creds).setRemote(remote).call();
		
	}
}
