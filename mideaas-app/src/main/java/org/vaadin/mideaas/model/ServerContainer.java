package org.vaadin.mideaas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.vaadin.data.util.BeanItemContainer;

public class ServerContainer extends BeanItemContainer<Server> implements
        Serializable {
    
    private static ServerContainer sc;
    
    public ServerContainer() throws InstantiationException,
            IllegalAccessException {
        super(Server.class);
    }
    
    public static void addServer(String ip, List<String> engines) {
    	if (sc == null) {
    		try {
    			sc = new ServerContainer();
    		} catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
    	}
    	Server p = new Server();
    	p.setIP(ip);
    	p.setEngines(engines);
    	System.out.println(ip + " and " + engines.toString());
    	sc.addItem(p);
    }
    
    public static Server getServer(String ip) {
    	Server server = null;
    	for (Server p : sc.getItemIds()) {
    		if (p.getIP().matches(ip)) {
    			server = p;
    		}
    	}
    	return server;
    }
    
    public static synchronized void removeServer(String serverIP) {
    	for (Server server : sc.getAllItemIds()) {
    		if (server.getIP().matches(serverIP)) {
    			sc.removeItem(server);
    		}
    	}
    }
    
    public static ServerContainer getServerContainer() {
    	return sc;
    }
    
    public static Server getFirstServer() {
    	Server server = null;
    	try {
    		server = sc.getIdByIndex(0);
    	} catch (NullPointerException e) {
    		server = null;
    	}
    	return server;
    }
    
    public static List<String> getServerEngines(String serverIP) {
    	List<String> engines = new ArrayList<String>();
    	try {
    		for (Server server : sc.getItemIds()) {
    			System.out.println(server.getEngines());
    			if (server.getIP().matches(serverIP)) {
    				engines = server.getEngines();
    			}
    		}
    	} catch (NullPointerException e) {
    		engines = null;
    	}
    	return engines;
    }
}