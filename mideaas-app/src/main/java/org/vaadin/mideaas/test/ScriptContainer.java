package org.vaadin.mideaas.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.vaadin.mideaas.app.MideaasTest;
import org.vaadin.mideaas.model.XmlTestWriter;

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
        Random r = new Random(0);
        try {
            c = new ScriptContainer();
            for (int i = 0; i < 2; i++) {
                Script p = new Script();
                p.setName(names[i]);
                p.setLocation(locations[r.nextInt(locations.length)]);
                p.setDescription(descriptions[r.nextInt(descriptions.length)]);
                p.setResult("NOT RUN");
                p.setEngine("robotEngine");
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
    	Script p = new Script();
    	
    	p.setName((String)testData.get(0));
        p.setLocation((String)testData.get(1));
        p.setDescription((String)testData.get(2));
        p.setEngine((String)testData.get(3));
        p.setResult("NOT RUN");
        p.setCheck(false);
        p.setNotes("This test has not been executed yet");
    	c.addItem(p);
    	
    	XmlTestWriter.WriteTestsToXml();
    
    	return c;
    }
    
    public static void addTestObjectToContainer(Script script) {
    	if (c == null) {
    		try {
    			c = new ScriptContainer();
    		} catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
    	}
    	c.addItem(script);
    }
    
    public static void setScriptContainer(ScriptContainer container) {
    	c = container;
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
    	XmlTestWriter.WriteTestsToXml();
    }
    
    public static synchronized Script getScriptFromContainer(String scriptName) {
    	Script item = null;
    	for (Script p : (List<Script>) c.getItemIds()) {
    		if (p.getName().matches(scriptName)) {
    			item = p;
    		}
    	}
    	return item;
    }
    
    public static synchronized void removeScriptFromContainer(String testName) {
    	for (Script item : c.getAllItemIds()) {
    		if (item.getName().matches(testName)) {
    			c.removeItem(item);
    			break;
    		}
    	}
    	XmlTestWriter.WriteTestsToXml();
    }
    
    public static synchronized void updateScriptCheckValue(HashSet<Object> markedRows) {
    	for (Script item : c.getAllItemIds()) {
    		if (markedRows.contains(item.getName())) {
    			item.setCheck(true);
    		} else {
    			item.setCheck(false);
    		}
    	}
    	XmlTestWriter.WriteTestsToXml();
    }
}







