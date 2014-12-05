package org.vaadin.mideaas.app.maven;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.maven.shared.invoker.InvocationResult;
import org.vaadin.mideaas.app.UserSettings;
import org.vaadin.mideaas.app.java.VaadinProject;
import org.vaadin.mideaas.app.maven.MavenTask.LogListener;

public class Builder {

	public interface BuildListener {
		public void buildStarted(List<String> goals);
		public void buildFinished(boolean success);
		public void buildCancelled();
	}

	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private MavenTask task;

	private final VaadinProject project;
	private final UserSettings settings;
	private CopyOnWriteArrayList<BuildListener> listeners = new CopyOnWriteArrayList<>();

	public Builder(VaadinProject project, UserSettings settings) {
		this.project = project;
		this.settings = settings;
	}
	
	public synchronized void addBuildListener(BuildListener li) {
		listeners.add(li);
	}
	
	public synchronized void removeBuildListener(BuildListener li) {
		listeners.remove(li);
	}

	public void build(List<String> goals, String buildDir, LogListener listener) {	
		System.out.println("BUILD! " + goals +" - " + buildDir);
		String writeError = null;
		fireBuildStarted(goals);
		synchronized (this) {
			writeError = writeToDisk();
			if (writeError==null) {
				doBuild(goals, buildDir, "safari" /* TODO  */, listener);
				return;
			}
		}
		fireBuildFinished(false);
	}
	
	
	
	private String writeToDisk() {
		try {
			project.writeToDisk();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}

	private void doBuild(List<String> goals, String buildDir, String userAgent, LogListener listener) {
		Properties props = new Properties();
		props.setProperty("projectDirectory", project.getProjectDir().getAbsolutePath());
		//compiles google appengine war
		if (settings.compileGae){
			
			//Downloads all the maven dependencies
			//mvn dependency:copy-dependencies			

			//goals.add(0,"dependency:copy-dependencies");
			
			props.setProperty("GAECompile", "true");
			if (userAgent!=null) {
				props.setProperty("mideaas.user.agent", userAgent);
			}
		}else{
			if (buildDir!=null) {
				props.setProperty("alt.build.dir", buildDir);
			}
			if (userAgent!=null) {
				props.setProperty("mideaas.user.agent", userAgent);
			}
		}
		
		task = new MavenTask(project.getPomXmlFile(), goals, props, listener);
//		task.setLoggingEnabled(true);

		executor.submit(new Runnable() {
			@Override
			public void run() {
				InvocationResult result = task.call();
				boolean success = result != null && result.getExitCode() == 0;
				fireBuildFinished(success);
			}
		});
	}
	
	private void fireBuildFinished(boolean success) {
		// Doesn't need to fire in a different thread because this is always
		// triggered by a background thread, never a Vaadin UI server visit.
		for (BuildListener li : listeners) {
			li.buildFinished(success);
		}
	}
	
	private void fireBuildStarted(List<String> goals) {
		for (BuildListener li : listeners) {
			li.buildStarted(goals);
		}
	}

	public void cancel() {
		synchronized (this) {
			executor.shutdownNow();
			executor = Executors.newSingleThreadExecutor();
		}
	}
}
