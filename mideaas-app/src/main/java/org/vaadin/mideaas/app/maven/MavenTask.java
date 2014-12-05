package org.vaadin.mideaas.app.maven;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.vaadin.mideaas.app.java.MavenUtil;

/**
 * For running a Maven task in a separate thread.
 * 
 */
public class MavenTask implements Callable<InvocationResult> {
	
	public interface LogListener {
		public void newLine(String line);
	}

	private final File pomXml;
	private final List<String> goals;
	private final LogListener listener;
	private final Properties properties;
	private InvocationResult result;

	public MavenTask(File pomXml, List<String> goals, Properties properties, LogListener listener) {
		this.pomXml = pomXml;
		this.goals = goals;
		this.properties = properties;
		this.listener = listener;
	}

	private void runMaven() {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(pomXml);
		request.setGoals(goals);

		InvocationOutputHandler handler;
		if (listener!=null) {
			handler = new InvocationOutputHandler() {
				@Override
				public void consumeLine(String li) {
//					System.out.println("Maven log: " + li);
					logLine(li);
				}
			};

		} else {
			// Empty handler. The default handler would output to System.out,
			// and we don't want that. (?)
			handler = new InvocationOutputHandler() {
				@Override
				public void consumeLine(String li) {
					// Nothing.
				}
			};
		}
		request.setOutputHandler(handler);
		request.setErrorHandler(handler);

		request.setProperties(properties);

		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(MavenUtil.getMavenHome());

		try {
			InvocationResult res = invoker.execute(request);
			setResult(res);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}
	}

	private synchronized void logLine(String li) {
		listener.newLine(li);
	}

	synchronized private void setResult(InvocationResult result) {
		this.result = result;
	}

	@Override
	public InvocationResult call() {
		runMaven();
		return result;
	}
}
