package org.vaadin.mideaas.app.maven;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.maven.shared.invoker.InvocationResult;
import org.vaadin.mideaas.app.UserSettings;
import org.vaadin.mideaas.app.VaadinProject;
import org.vaadin.mideaas.app.maven.MavenTask.LogListener;

public class Builder {

	public interface BuildListener {
		public void buildStatusChanged(BuildStatus status);
	}
	
	public enum Status {
		NEVER_RAN,
		RUNNING,
		CANCELLED, // TODO CANCELLED not used
		SUCCEEDED,
		FAILED
	}
	
	public class BuildStatus {
		public final Status status;
		public final String errorMessage;
		public final List<String> goals;
		private BuildStatus(Status status, String errorMessage, List<String> goals) {
			this.status = status;
			this.errorMessage = errorMessage;
			this.goals = goals;
		}
	}
	
	private BuildStatus status = new BuildStatus(Status.NEVER_RAN, null, null);

	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private MavenTask task;

	private final VaadinProject project;
	private CopyOnWriteArrayList<BuildListener> listeners = new CopyOnWriteArrayList<>();

	public Builder(VaadinProject project) {
		this.project = project;
	}
	
	public synchronized void addBuildListener(BuildListener li) {
		listeners.add(li);
	}
	
	public synchronized void removeBuildListener(BuildListener li) {
		listeners.remove(li);
	}
	
	public synchronized BuildStatus getStatus() {
		return status;
	}

	public void build(List<String> goals, String buildDir, UserSettings settings, LogListener listener) {
		synchronized (this) {
			if (status.status == Status.RUNNING) {
				return;
			}
		}
		System.out.println("BUILD! " + goals +" - " + buildDir);
		setStatus(new BuildStatus(Status.RUNNING, null, goals));
		synchronized (this) {
			try {
				project.writeToDisk();
				doBuild(goals, buildDir, settings, listener);
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setStatus(new BuildStatus(Status.FAILED, "File error", goals));
	}
	
	private void setStatus(BuildStatus status) {
		synchronized (this) {
			this.status = status;
		}
		fireBuildStatusChanged(status);
	}

	private void doBuild(final List<String> goals, String buildDir, UserSettings settings, LogListener listener) {
		Properties props = new Properties();
		props.setProperty("projectDirectory", project.getProjectDir().getAbsolutePath());
		//compiles google appengine war
		
		// TODO: these settings are not taken into account in regular Vaadin apps!
		// There's not mideaas.user.agent property in their pom.xml etc...
		
		String userAgent = settings.userAgent;
		
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

				if (success) {
					setStatus(new BuildStatus(Status.SUCCEEDED, null, goals));
				} else {
					setStatus(new BuildStatus(Status.FAILED, "Failed", goals));
				}
			}
		});
	}
	
	private void fireBuildStatusChanged(BuildStatus status) {
		for (BuildListener li : listeners) {
			li.buildStatusChanged(status);;
		}
	}

	public void cancel() {
		synchronized (this) {
			executor.shutdownNow();
			executor = Executors.newSingleThreadExecutor();
		}
	}
}
