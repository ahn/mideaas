package org.vaadin.mideaas.model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import org.vaadin.mideaas.app.MideaasTest;
import org.vaadin.mideaas.test.ScriptContainer;

public class XmlRpcRunnable implements Runnable {
	private final String server;
	private final String testName;
	private final Map<String, String> map;
	private final int i; 
	
	XmlRpcRunnable(String server, String testName, Map<String, String> map, int i) {
		this.server = server;
		this.testName = testName;
		this.map = map;
		this.i = i;
		System.out.println(this.map.toString());
	}
	
	@Override
	public void run() {
		Object result = null;
		
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(this.server));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			System.out.println(this.map.toString());
			
			Map<String, String> newmap = this.setupTest(this.map, this.testName);
        	
			result = client.execute("executeTestCase", new Object[] {newmap});
			
			ScriptContainer.updateResult((HashMap<String, String>) result, this.testName);
			
			MideaasTest.updateTable();
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("result", "b");
			map.put("notes", ex.getStackTrace().toString());
		}
		System.out.println("Finished thread " + this.testName);
	}
	
	
	private Map<String, String> setupTest(Map<String, String>map, String testName) {
		
		testName = testName + ".txt";
		String name = map.get("testCaseName");
		name = name + Integer.toString(i);
		map.remove("testCaseName");
		Map<String, String> newmap = new HashMap<String, String>();
		for( String key : map.keySet() ) {
			newmap.put( key, map.get( key ) );
		}
		newmap.put("testCaseName", name);
		newmap.put("scripts", testName);
		
		return newmap;
	}
}
