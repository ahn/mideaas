package org.vaadin.mideaas.model;

import java.io.Serializable;
import java.util.List;


public class Server implements Serializable {
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
        this.details = details;
    }
}