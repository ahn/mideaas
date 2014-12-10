package org.vaadin.mideaas.app.test;

import java.io.Serializable;
import java.util.List;


public class Server implements Serializable {
	private static final long serialVersionUID = 1L;
	// class to preserve known FNTS servers during runtime
    private String ip = "";
    private List<String> engines = null;
    private String details = "";
    
    public String getIP() {
        return ip;
    }
    public void setIP(String ip) {
        this.ip = ip;
    }
    public List<String> getEngines() {
        return engines;
    }
    public void setEngines(List<String> engines) {
    	for (String e : engines) {
    		e = e.trim();
    	}
        this.engines = engines;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
    	try {
    		if (!details.matches("null")) {
    			this.details = details;
    		} else {
    			this.details = "Details were not found, the server might be outdated";
    		}
    	} catch (Exception e) {
    		this.details = "Details were not found, the server might be outdated";
    	}
        
    }
}