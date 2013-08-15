package org.vaadin.mideaas.tests;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;

public class ScriptContainer extends BeanItemContainer<Script> implements
        Serializable {

	static final String[] names = { "First_test", "Second_test" };
	static final String[] locations = { "/home/nikokorhonen/repotesti/" };
	static final String[] descriptions = { "Pre-created test" };
	static final String[] results = { "NN" };
    
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
                p.setName(names[r.nextInt(names.length)]);
                p.setLocation(locations[r.nextInt(locations.length)]);
                p.setDescription(descriptions[r.nextInt(descriptions.length)]);
                p.setResult("NN");
                p.setCheck(false);
                c.addItem(p);
            }
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
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
        p.setResult("NN");
        p.setCheck(false);
    	c.addItem(p);
    
    	return c;
    }
    
}