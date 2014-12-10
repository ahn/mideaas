package org.vaadin.mideaas.app.maven;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import org.vaadin.mideaas.app.maven.JettyUtil.JettyException;

import com.vaadin.server.VaadinServletService;

public class JettyServer {
	
	public interface JettyServerListener {
		public void jettyServerStatusChanged(JettyStatus status);
	}

	private CopyOnWriteArrayList<JettyServerListener> listeners = new CopyOnWriteArrayList<JettyServerListener>();
	
	public enum Status {
		STOPPED,
		RUNNING
	}
	
	public class JettyStatus {
		public final Status status;
		public final int port;
		public JettyStatus(Status status, int port) {
			this.status = status;
			this.port = port;
		}
	}
	
	private JettyStatus status = new JettyStatus(Status.STOPPED, -1);

	private final File pomXml;
	private final String contextPath;
	
	public JettyServer(File pomXml, String contextPath) {
		this.pomXml = pomXml;
		this.contextPath = contextPath;
	}
	
	public synchronized JettyStatus getStatus() {
		return status;
	}
	
	public synchronized void addListener(JettyServerListener li) {
		listeners.add(li);
	}
	
	public synchronized void removeListener(JettyServerListener li) {
		listeners.remove(li);
	}
	
	public void start(LogView logView) {
		int port;
		synchronized (this) {
			if (status.status == Status.RUNNING) {
				return;
			}
			port = JettyUtil.runJetty(pomXml, contextPath, "target", logView);
			if (port < 0) {
				// TODO setStatus(FAILED) ?
				return;
			}
		}
		setStatus(new JettyStatus(Status.RUNNING, port));
	}
	
	public synchronized void stop(LogView logView) {
		synchronized (this) {
			if (status.status == Status.STOPPED) {
				return;
			}
			try {
				JettyUtil.stopJetty(status.port, pomXml, contextPath, logView);
			}
			catch (JettyException e) {
				e.printStackTrace();
				// TODO setStatus(FAILED) ?
				return;
			}
		}
		setStatus(new JettyStatus(Status.STOPPED, -1));
	}
	
	private void setStatus(JettyStatus status) {
		synchronized (this) {
			this.status = status;
		}
		fireStatusChanged(status);
	}

	private void fireStatusChanged(JettyStatus status) {
		for (JettyServerListener li : listeners) {
			li.jettyServerStatusChanged(status);
		}
	}

	public String getContextPath() {
		return contextPath;
	}

}
