package org.vaadin.mideaas.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.vaadin.mideaas.app.MideaasTest;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;

public class ScriptContainer extends BeanItemContainer<Script> implements
        Serializable {

	static final String[] names = { "First_test", "Second_test" };
	static final String[] locations = { "tests/" };
	static final String[] descriptions = { "Pre-created test" };
	static final String[] results = { "NOT RUN" };
    
    private static ScriptContainer c;
    
    public ScriptContainer() throws InstantiationException,
            IllegalAccessException {
        super(Script.class);
    }

    public static ScriptContainer createWithTestData() {
        //ScriptContainer c = null;
        Random r = new Random(0);
        try {
            c = new ScriptContainer();
            for (int i = 0; i < 2; i++) {
                Script p = new Script();
                p.setName(names[i]);
                p.setLocation(locations[r.nextInt(locations.length)]);
                p.setDescription(descriptions[r.nextInt(descriptions.length)]);
                p.setResult("NOT RUN");
                p.setCheck(false);
                p.setNotes("This test has not been executed yet");
                c.addItem(p);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return c;
    }
    
    public static ScriptContainer addTestToContainer(List testData) {
    	//ScriptContainer c = null;
    	Script p = new Script();
    	
    	p.setName((String)testData.get(0));
        p.setLocation((String)testData.get(1));
        p.setDescription((String)testData.get(2));
        p.setResult("NOT RUN");
        p.setCheck(false);
        p.setNotes("This test has not been executed yet");
    	c.addItem(p);
    
    	return c;
    }
    
    
    public static synchronized ScriptContainer getContainer() {
    	return c;
    }
    
    
    public static synchronized ScriptContainer SetRunnableTests(List<String> testNames) {
    	try {
    		System.out.println("Trying to mark runnable tests");
    		for (String name : testNames) {
    			for (Script item : (List<Script>) c.getItemIds()) {
    				if (item.getName().equals(name)) {
    					//change the status to RUNNING
    					item.setResult("RUNNING");
    					break;
    				}
    			}
    		}
    		MideaasTest.updateTable();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return c;
    }
    
    
    public static synchronized void updateResult(HashMap<String, String> map, String testName) {
    	for (Script p : c.getItemIds()) {
    		if (p.getName().equals(testName)) {
    			//found the correct test
    			p.setNotes(map.get("notes"));
    			if (map.get("result").equals("p")) {
    				p.setResult("PASS");
    			} else if (map.get("result").equals("f")) {
    				p.setResult("FAIL");
    			} else if (map.get("result").equals("b")) {
    				p.setResult("BLOCKED");
    			}
    			System.out.println("Set result " + p.getResult() + " for " + testName);
    			break;	//no need to look for more tests
    		}
    	}
    }
    
    public static synchronized Script getScriptFromContainer(String scriptName) {
    	Script item = null;
    	for (Script p : (List<Script>) c.getItemIds()) {
    		//System.out.println("found item " + p.getName() + " from container");
    		//System.out.println("checking if item '" + p.getName() + "' is '" + scriptName + "'...");
    		if (p.getName().matches(scriptName)) {
    			//System.out.println("found it!");
    			item = p;
    		}
    	}
    	return item;
    }
}







