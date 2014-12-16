package org.vaadin.mideaas.app.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.vaadin.mideaas.app.MideaasConfig;
import org.vaadin.mideaas.app.VaadinProject;

import com.vaadin.ui.Notification;

public class XmlRpcContact {
	
	volatile List<String> scriptList = new ArrayList<String>();
	
	public static String ping(String server) {
		
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(server));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

        	HashMap<String, String> result = null;
            result = (HashMap<String, String>)client.execute("ping", new Object[] {"guest"});
            return (String)result.get("ping");
        }
        catch ( Exception ex ) {
        	return "Connection failed";
        }
	}
	
	//executes all tests at once, no threading
	public static Object executeTests(String server, Map<String, String> map) {
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
	
	public static void executeParallelTests(String server, Map<String, String> map, int NTHREADS, final MideaasTest mideaasTest, VaadinProject project) {
		/*
		 * the executor runs all the tests in separate threads, making better use of FNTS
		 * this way the page doesn't have to hang until the tests have been executed, they will be reported the instant
		 * they are finished
		 * NTHREADS is the number of possible threads 
		 */
		List<String> list = Arrays.asList(map.get("scriptNames").split("\\s*,\\s*"));
		map.remove("scriptNames");
		
		List<HashMap<String, String>> blocks = CreateTestBlocks(list, project);
		
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
		
		ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
		ScriptContainer.SetRunnableTests(list);
		mideaasTest.updateTable();
		int i = 1;
		for (HashMap<String, String> block : blocks) {

			System.out.println("index is " + i);
			//String script = getScriptFromFile(test); TODO
			String script = " ";
			
			map.put("testingEngine", block.get("engine"));
			
			Runnable worker = new XmlRpcRunnable(server, block.get("scriptNames"), script, map, i, mideaasTest);
		    executor.execute(worker);
			i++;
			
		}
		// This will make the executor to not accept new threads
	    // and finish all existing threads in the queue
	    executor.shutdown();
	}
	
	public static synchronized Object getServerDetails(String server, String checkDetail) {
		Object result = null;
		Map<String, String> map = new HashMap<String, String>();
		//map.put("key", "value");
		try{
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(server));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			if (checkDetail.matches("engines")) {
        	    System.out.println("checking engines");
        	    map.put("getdetails", "false");
                //result = client.execute("getServerDet", new Object[] {map});
			} else if (checkDetail.matches("details")) {
				System.out.println("checking server details");
				map.put("getdetails", "true");
                //result = client.execute("getServerDetails", new Object[] {map});
			} else {
				System.out.println("This shouldn't have happened...");
				Map<String, String> resmap = new HashMap<String, String>();
				resmap.put("error", "something went wrong");
				result = resmap;
			}
			result = client.execute("getServerDetails", new Object[] {map});
        } catch ( Exception ex ) {
        	System.out.println((String)result);
        	Map<String, String> resmap = new HashMap<String, String>();
        	resmap.put("error", "Something went wrong: " + ex.toString());
        	result = resmap;
        }
		System.out.println(result.toString());
		return result; 
	}
	
	
	public static String getScriptFromFile(String scriptName, VaadinProject project) {
		System.out.println(scriptName);
		Script item = ScriptContainer.getScriptFromContainer(scriptName);
		System.out.println(item.toString());
		String script = "";
		try {
			String path = project.getWorkDirFile() + "/" + item.getLocation() + scriptName + ".txt"; //TODO: project name needs to be dynamic
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
	
	private static List<HashMap<String, String>> CreateTestBlocks(List<String> list, VaadinProject project) {
		Script script;
		String engine;
		boolean engineFound = false;
		List<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
		
		for(String scriptName : list){
			script = ScriptContainer.getScriptFromContainer(scriptName);
			engine = script.getEngine();
			
			if(maps.isEmpty()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("engine", engine);
				map.put("scriptNames", script.getName());
				maps.add(map);
			} else {
				for(HashMap<String, String> map: maps){
					if(map.get("engine").matches(engine)) {
						String scriptNames = map.get("scriptNames");
						map.remove("scriptNames");
						scriptNames = scriptNames + ", " + script.getName();
						map.put("scriptNames", scriptNames);
						engineFound = true;
						break;
					}
				}
				if(engineFound == false){
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("engine", engine);
					map.put("scriptNames", script.getName());
					maps.add(map);
				}
				engineFound = false;
			}
			
			if (maps.size() == 1){
				System.out.println("testing if we get through");
				maps.get(0).put("script", getScriptFromFile(maps.get(0).get("scriptNames"), project));
			}
		}
		System.out.println(maps);
		return maps;
	}
}







