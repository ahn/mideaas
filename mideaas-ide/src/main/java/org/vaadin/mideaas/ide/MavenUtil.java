package org.vaadin.mideaas.ide;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.vaadin.mideaas.ide.model.User;

// TODO: is this class needed anywhere?

public class MavenUtil {


	private static File mavenHome;
	
	private static final InvocationOutputHandler emptyHandler = new InvocationOutputHandler() {
		@Override
		public void consumeLine(String arg0) {
			// Nothing.
		}
	};
	
	public static void setMavenHome(File mavenHome) {
		MavenUtil.mavenHome = mavenHome;
	}
	
	public static File getMavenHome() {
		return mavenHome;
	}
	
	public static void execute(File dir, String command, Properties props, boolean logToStdOut) throws MavenInvocationException {
		execute(dir, Collections.singletonList(command), props, logToStdOut);
	}
	
	public static void execute(File dir, List<String> commands, Properties props, boolean logToStdOut) throws MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setBaseDirectory(dir);
		request.setGoals(commands);
		if (props!=null) {
			request.setProperties(props);
		}
		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(mavenHome);
		
		if (!logToStdOut) {
			request.setOutputHandler(emptyHandler);
		}
		
		invoker.execute(request);
	}

	public static String getClassPath(File projectDir) {
		
		InvocationRequest request = new DefaultInvocationRequest();
		request.setBaseDirectory(projectDir);
		request.setGoals(Collections.singletonList("dependency:build-classpath"));
		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(mavenHome);

		ClassPathListener cpl = new ClassPathListener();
		invoker.setOutputHandler(cpl);

		try {
			invoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}

		return cpl.classPath;
	}

	private static final class ClassPathListener implements
			InvocationOutputHandler {
		private boolean nextIsIt = false;
		private String classPath;

		@Override
		public void consumeLine(String msg) {
			if (classPath != null) {
				return;
			}
			if (nextIsIt) {
				classPath = msg;
			} else if ("[INFO] Dependencies classpath:".equals(msg)) {
				nextIsIt = true;
			}
		}
	}
	
	public static String targetDirFor(User u) {
		
		//System.out.println( "userID: " + u.getUserId());
		
		return "target-"+u.getUserId();
	}
	
}
