package org.vaadin.mideaas.app.maven;

public class JettyConfiguration {
	public final int port;
	public final int stopPort;
	public final String stopKey;
	public final int scanInterval;
	public final String contextPath;
	public JettyConfiguration(int port, int stopPort, String stopKey,
			int scanInterval, String contextPath) {
		this.port = port;
		this.stopPort = stopPort;
		this.stopKey = stopKey;
		this.scanInterval = scanInterval;
		this.contextPath = contextPath;
	}
	
}
