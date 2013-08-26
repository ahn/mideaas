package org.vaadin.mideaas.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.vaadin.mideaas.app.MideaasConfig;
import org.vaadin.mideaas.test.ScriptContainer;
import org.vaadin.mideaas.test.Script;

import com.vaadin.ui.Notification;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XmlRpcContact {
	
	volatile List<String> scriptList = new ArrayList<String>();
	
	public String ping(String server) {
		
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(server));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

        	Object result = null;
            result = client.execute("ping", new Object[] {"guest"});
            return (String)result;
        }
        catch ( Exception ex ) {
        	ex.printStackTrace();
        	return "Connection failed";
        }
	}
	
	//executes all tests at once, no threading
	public Object executeTests(String server, Map<String, String> map) {
    	Object result = null;
		
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(server));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
        	
            result = client.execute("executeTestCase", new Object[] {map});
        }
        catch ( Exception ex ) {
        	ex.printStackTrace();
        }
		return result;
	}
	
	public void executeParallelTests(String server, Map<String, String> map, int NTHREADS) {
		/*
		 * the executor runs all the tests in separate threads, making better use of FNTS
		 * this way the page doesn't have to hang until the tests have been executed, they will be reported the instant
		 * they are finished
		 * NTHREADS is the number of possible threads 
		 */
		List<String> list = Arrays.asList(map.get("scripts").split("\\s*,\\s*"));
		map.remove("scripts");
		
		//commented just in case it's needed for some reason
		/*if (list.size()/NTHREADS > 1) {
			int tests = (int)Math.ceil(list.size()/NTHREADS);	//how many tests one executor gets
			String cases = "";
			int i = 0; //counter towards how many tests one executor gets
			for (int j = 0; j <= list.size(); j++) {
				if (j == list.size()) {
					//no more tests on the list, add last tests to the runnable list
					scriptList.add(cases);
				} else if (i < tests) {
					if (cases == "") {
						cases = list.get(j);
					} else {
						cases = cases + ", " + list.get(j);
					}
					i++;
				} else {
					scriptList.add(cases);
					cases = "";
					i = 0;
				}
			}
		} else {
			scriptList = list;
		}*/
		
		//List<Thread> threads = new ArrayList<Thread>();
		
		ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
		ScriptContainer.SetRunnableTests(list);
		int i = 1;
		for (String test : list) {

			System.out.println("index is " + i);
			String script = this.getScriptFromFile(test);
			
			Runnable worker = new XmlRpcRunnable(server, test, script, map, i);
		    executor.execute(worker);
			i++;
			
		}
		// This will make the executor accept no new threads
	    // and finish all existing threads in the queue
	    executor.shutdown();
	}
	
	public synchronized Object getServerDetails(String server) {
		Object result = null;
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "value");
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(server));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
        	
            result = client.execute("getServerDetails", new Object[] {map});
        }
        catch ( Exception ex ) {
        	ex.printStackTrace();
        	Map<String, String> resmap = new HashMap<String, String>();
        	resmap.put("error", "Something went wrong: " + ex.toString());
        	result = resmap;
        }
		System.out.println(result.toString());
		return result; 
	}
	
	public String getScriptFromFile(String scriptName) {
		System.out.println(scriptName);
		Script item = ScriptContainer.getScriptFromContainer(scriptName);
		System.out.println(item.toString());
		String script = "";
		try {
			String path = MideaasConfig.getProjectsDir() + "test/" + item.getLocation() + scriptName + ".txt"; //TODO: project name needs to be dynamic
			BufferedReader br = new BufferedReader(new FileReader(path));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append("\n");
					line = br.readLine();
				}
				script = sb.toString();
			} catch (IOException e) {
				Notification.show("Whoops", "Reading from a file failed", Notification.Type.ERROR_MESSAGE);
				e.printStackTrace();
			} finally {
				br.close();
			}
		} catch (Exception e) {
			Notification.show("Whoops", "Reading from a file failed", Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		return script;
	}
}







