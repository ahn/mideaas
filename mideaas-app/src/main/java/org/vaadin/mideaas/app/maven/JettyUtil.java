package org.vaadin.mideaas.app.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.vaadin.mideaas.app.maven.MavenTask.LogListener;
import org.vaadin.mideaas.ide.IdeProject;
import org.xml.sax.SAXException;

import com.vaadin.ui.Notification;

public class JettyUtil {
	
	@SuppressWarnings("serial")
	public static class JettyException extends RuntimeException {
		public JettyException(String msg) {
			super(msg);
		}
	}
	
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
	 * @return port, or -1 if could not start
	 */
	synchronized public static int runJetty(File pomXml, String contextPath, String buildDir, LogListener listener) {
		int port = getAvailablePort();
		int stopPort = getAvailableStopPort();
		portsAvailable.remove(port);
		stopPortsAvailable.remove(stopPort);
		
		portsInUse.put(port, stopPort);

		Properties props = new Properties();
		props.setProperty("jetty.port", "" + port);

		JettyConfiguration cfg = new JettyConfiguration(port, stopPort, STOP_KEY, 2, contextPath);
		

		try {
			File tempPomXml = writeTempPomXmlForRunning(pomXml, cfg);
			MavenTask task = new MavenTask(tempPomXml.getAbsoluteFile(), GOAL_JETTY_RUN, props, listener);
			executor.submit(task);
			return port;
		} catch (IOException | ParserConfigurationException | SAXException | TransformerFactoryConfigurationError | TransformerException e) {
			Notification.show("Could not run Jetty", Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
			return -1; // TODO ???
		}
		
		
	}

	
	private static File writeTempPomXmlForRunning(File pomXml, JettyConfiguration cfg) 
			throws IOException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		byte[] bytes = Files.readAllBytes(pomXml.toPath());
		
		PomXml pom = new PomXml(new String(bytes, "UTF-8"));
		
		boolean success = pom.configureJetty(cfg);
		if (!success) {
			throw new IOException(); // ?
		}
		
		File tmp = new File(pomXml.getParentFile(), "jetty-run-pom.xml");
		System.out.println("temp pom.xml: " + tmp);
		Files.write(tmp.toPath(), pom.getAsString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		return tmp;
	}

	synchronized public static void stopJetty(int port, File pomXml, String contextPath, LogListener listener) {
		Integer stopPort = portsInUse.remove(port);
		if (stopPort==null) {
			throw new JettyException("No Jetty running in port "+port+" (that I know of...)");
		}
		portsAvailable.add(port);
		stopPortsAvailable.add(stopPort);
		
		File jettyPomXml = new File(pomXml.getParentFile(), "jetty-run-pom.xml");
		
		executor.submit(new MavenTask(jettyPomXml.getAbsoluteFile(), GOAL_JETTY_STOP, new Properties(), listener));
		
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


	public static String contextPathFor(IdeProject project) {
		return "/apps/"+project.getName();
	}
	
	

}
