package org.vaadin.mideaas.model;

/**
 * 
 * Not synchronized because meant to be accessed only by the main (Vaadin UI) thread.
 *
 */
public class UserSettings {
	public String userAgent;
	public Boolean easiCloudsFeaturesTurnedOn;
	public boolean paasDeployTurnedOn;
	public boolean compileGae;
}
