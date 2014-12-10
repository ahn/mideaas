package org.vaadin.mideaas.app.test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.vaadin.mideaas.app.VaadinProject;

public class XmlRpcRunnable implements Runnable {
	private final String server;
	private final String testName;
	private final String script;
	private final Map<String, String> map;
	private final int i; 
	private final MideaasTest mideaasTest;
	private final VaadinProject project;
	
	XmlRpcRunnable(String server, String testName, String script, Map<String, String> map, int i, MideaasTest mideaasTest) {
		this.server = server;
		this.testName = testName;
		this.script = script;
		this.map = map;
		this.i = i;
		System.out.println(this.map.toString());
		this.mideaasTest = mideaasTest;
		this.project = mideaasTest.getProject();
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
        	
			System.out.println(newmap);
			result = client.execute("executeTestCase", new Object[] {newmap});
			
			for(String test : this.testName.split(", ")){
				ScriptContainer.updateResult((HashMap<String, String>) result, test, this.project);
			}
			
			mideaasTest.updateTable();
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("result", "b");
			map.put("notes", ex.getStackTrace().toString());
		}
		System.out.println("Finished thread " + this.testName);
	}
	
	
	private Map<String, String> setupTest(Map<String, String> map, String testName) {
		
		String name = map.get("testCaseName");
		System.out.println("testcasename: " + name);
		name = name + Integer.toString(i);
		Map<String, String> newmap = new HashMap<String, String>();
		for( String key : map.keySet() ) {
			newmap.put( key, map.get( key ) );
		}
		newmap.remove("testCaseName");
		newmap.put("testCaseName", name);
		newmap.put("projectName", this.project.getName());
		newmap.put("scriptNames", testName);  	// script file names
		newmap.put("script", this.script);	// the test script
		
		return newmap;
	}
}
