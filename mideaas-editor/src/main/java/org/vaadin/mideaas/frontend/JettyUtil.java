package org.vaadin.mideaas.frontend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.vaadin.mideaas.frontend.MavenTask.LogListener;
import org.vaadin.mideaas.model.SharedProject;

public class JettyUtil {
	
	private static final List<String> GOAL_JETTY_RUN = Arrays
			.asList(new String[] { "jetty:stop", "jetty:run" });

	private static final List<String> GOAL_JETTY_STOP = Arrays
			.asList(new String[] { "jetty:stop" });

	private static final String STOP_KEY = "heilopeta";
	
	// TODO: what if we run out of ports?
	
	private static int PORT_FIRST;
	private static int PORT_LAST;
	
	private static int STOP_PORT_FIRST;
	private static int STOP_PORT_LAST;
	
	private static TreeSet<Integer> portsAvailable = new TreeSet<Integer>();
	private static TreeSet<Integer> stopPortsAvailable = new TreeSet<Integer>();
	
	public static void setPortRange(int first, int last) {
		PORT_FIRST = first;
		PORT_LAST = last;
		for (int p=PORT_FIRST; p<=PORT_LAST; p++) {
			portsAvailable.add(p);
		}
	}
	
	public static void setStopPortRange(int first, int last) {
		STOP_PORT_FIRST = first;
		STOP_PORT_LAST = last;
		for (int p=STOP_PORT_FIRST; p<=STOP_PORT_LAST; p++) {
			stopPortsAvailable.add(p);
		}
	}
	
	private static void setAllPortsOpen() {
		setPortRange(PORT_FIRST, PORT_LAST);
		setStopPortRange(STOP_PORT_FIRST, STOP_PORT_LAST);
	}
	
	/**
	 * key: port
	 * value: stopPort
	 */
	private static TreeMap<Integer, Integer> portsInUse =
			new TreeMap<Integer, Integer>();
	
	
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * 
	 * @param pomXml
	 * @param contextPath
	 * @return port
	 */
	synchronized public static int runJetty(File pomXml, String contextPath, String buildDir, LogListener listener) {
		int port = getAvailablePort();
		int stopPort = getAvailableStopPort();
		portsAvailable.remove(port);
		stopPortsAvailable.remove(stopPort);
		
		portsInUse.put(port, stopPort);
		
		Properties props = new Properties();
		props.setProperty("jetty.port", "" + port);
		
		// TODO: buildDir?
		props.setProperty("alt.build.dir", buildDir);
		
		props.setProperty("mideaas.jetty.stopPort", ""+stopPort);
		props.setProperty("mideaas.jetty.stopKey", STOP_KEY);
		props.setProperty("mideaas.jetty.contextPath", contextPath);
		MavenTask task = new MavenTask(pomXml.getAbsoluteFile(), GOAL_JETTY_RUN, props, listener);
//		task.setLoggingEnabled(true);
		executor.submit(task);
		
		return port;
	}

	
	synchronized public static void stopJetty(int port, File pomXml, String contextPath, LogListener listener) {
		Integer stopPort = portsInUse.remove(port);
		if (stopPort==null) {
			throw new IllegalArgumentException("No Jetty running in port "+port+" (that I know of...)");
		}
		portsAvailable.add(port);
		stopPortsAvailable.add(stopPort);
		
		Properties props = new Properties();
		props.setProperty("jetty.port", "" + port);
		props.setProperty("mideaas.jetty.stopPort", ""+stopPort);
		props.setProperty("mideaas.jetty.stopKey", STOP_KEY);
		props.setProperty("mideaas.jetty.contextPath", contextPath);
		
		executor.submit(new MavenTask(pomXml.getAbsoluteFile(), GOAL_JETTY_STOP, props, listener));
		
	}
	
	/**
	 * Stops all the Jetty instances, in a separate thread.
	 * 
	 * Runs jetty:stop for each stop port, whether there was Jetty running or not.
	 * This is not the cleanest solution...	
	 */
	synchronized public static void stopAllJettys() {
		InputStream is = JettyUtil.class.getClassLoader().getResourceAsStream("jettyutil/pom.xml");
		StringWriter writer = new StringWriter();
		File temp;
		try {
		IOUtils.copy(is, writer);
		String theString = writer.toString();
		temp = File.createTempFile("mideaas", "pom.xml");
		FileUtils.write(temp, theString);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		ExecutorService exer = Executors.newSingleThreadExecutor();
		for (int p=STOP_PORT_FIRST; p<=STOP_PORT_LAST; p++) {
			Properties props = new Properties();
			props.setProperty("mideaas.jetty.stopPort", ""+p);
			props.setProperty("mideaas.jetty.stopKey", STOP_KEY);
			MavenTask task = new MavenTask(temp, GOAL_JETTY_STOP, props, null);
			exer.submit(task);
		}
		exer.shutdown();
		
		setAllPortsOpen();
	}
		
	private static int getAvailablePort() {
		return portsAvailable.first();
	}
	
	private static int getAvailableStopPort() {
		return stopPortsAvailable.first();
	}


	public static String contextPathFor(SharedProject project) {
		return "/apps/"+project.getName();
	}
	
	

}
