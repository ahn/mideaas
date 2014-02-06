package org.vaadin.mideaas.model;

/**
 * 
 * Not synchronized because meant to be accessed only by the main (Vaadin UI) thread.
 *
 */
public class UserSettings {
	public String userAgent;
	public final Boolean easiCloudsFeaturesTurnedOn;
	public final boolean paasDeployTurnedOn;
	public final boolean compileGae;
	public final String coapsApiUri;
	
	public UserSettings(String apiUri, boolean easiCloudsFeaturesTurnedOn, boolean paasDeployTurnedOn, boolean compileGae){
		this.easiCloudsFeaturesTurnedOn = easiCloudsFeaturesTurnedOn; 
		this.paasDeployTurnedOn = paasDeployTurnedOn;
		this.compileGae = compileGae;
		this.coapsApiUri = apiUri;
	}
}
