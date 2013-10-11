package org.vaadin.mideaas.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vaadin.mideaas.model.Server;
import org.vaadin.mideaas.model.ServerContainer;
import org.vaadin.mideaas.model.XmlRpcContact;
import org.vaadin.mideaas.test.Script;
import org.vaadin.mideaas.test.ScriptContainer;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TestRunConfirmation extends Window {
	
	static final HashSet<Object> markedRows = new HashSet<Object>();
	static List<String> fntsServers = new ArrayList<String>();
	
	final Window confirmTests = new Window("Confirm runnable tests");
	
    final com.vaadin.ui.TextArea listTests = new com.vaadin.ui.TextArea("Selected tests");
	final com.vaadin.ui.TextField textCaseName = new com.vaadin.ui.TextField("Test run name");
    final com.vaadin.ui.ComboBox cmbEngine = new com.vaadin.ui.ComboBox("Testing engine");
	final com.vaadin.ui.TextField textTolerance = new com.vaadin.ui.TextField("Tolerance");
    final com.vaadin.ui.TextField textRuntimes = new com.vaadin.ui.TextField("Run # of times");
    final com.vaadin.ui.ComboBox cmbServer = new com.vaadin.ui.ComboBox("XMLRPC Server");
	
	protected Window newWindow(HashSet<Object> rows){
		markedRows.addAll(rows);
		
		//the test confirmation window
        confirmTests.setWidth("640px");
        confirmTests.setHeight("480px");
        
        //fields that contain the info needed for running the tests
        listTests.setRows(10);
        listTests.setColumns(25);
        listTests.setReadOnly(false);
        
        
        try {
        	//set the first server at startup
        	String first = ServerContainer.getFirstServer().getIP();
        	if (XmlRpcContact.ping(first).matches("pong")) {
        		cmbServer.addItem(first);
        		cmbServer.setValue(first);
        	} else {
        		//the first server didn't respond, looking for the next server...
        		for (Server server : ServerContainer.getServerContainer().getItemIds()) {
        			String ip = server.getIP();
        			if (XmlRpcContact.ping(ip).matches("pong")) {
                		cmbServer.addItem(ip);
                		cmbServer.setValue(ip);
                		break;
        			}
        		}
        	}
        	
        	System.out.println(cmbServer.getValue().toString());
        	for (String engine : ServerContainer.getServerEngines((String)cmbServer.getValue())){
        		cmbEngine.addItem(engine);
        	}
        } catch (NullPointerException e) {
        	//no servers to connect to, leaving the options empty
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
        listTests.setReadOnly(true);
        cmbServer.setImmediate(true);
        cmbServer.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					for (String engine : ServerContainer.getServerEngines((String)cmbServer.getValue())){
						cmbEngine.addItem(engine);
					}
				} catch (NullPointerException e) {
					//...why are you doing this to me, Jens?
					e.printStackTrace();
				}
			}
		});
        
        //buttons for confirmation window
        Button btnAccept = new Button("Run tests", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				if (markedRows.isEmpty()) {
						
				} else {
					// send test request to FNTS
					Map<String, String> map = new HashMap<String, String>();
					map.put("testCaseName", textCaseName.getValue());
			       	
					//adding test script names
					String tests = "";
					for (Iterator i = markedRows.iterator(); i.hasNext();) {
						Script item = (Script) i.next();
						if (tests == "") {
							tests = item.getName();
						} else {
							tests = tests + ", " + item.getName();
						}
					}
			        	
					map.put("scripts", tests);
					System.out.println(tests);
					map.put("engine", (String)cmbEngine.getValue());
					map.put("tolerance", (String)textTolerance.getValue());
					map.put("runtimes", (String)textRuntimes.getValue());
					map.put("gitRepository", "ironclad.labranet.jamk.fi:robot_testing_scripts");
					map.put("tag", "");
			       	
					XmlRpcContact.executeParallelTests((String)cmbServer.getValue(), map, MideaasConfig.getExecutorNumber());
					UI.getCurrent().removeWindow(confirmTests);
				}
			}
		});
        Button btnCancel = new Button("Cancel", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				// go back to main window
				UI.getCurrent().removeWindow(confirmTests);
			}
		});
        
        //create the confirmation window layout
        VerticalLayout textAreaLayout = new VerticalLayout();
        textAreaLayout.addComponent(textCaseName);
        textAreaLayout.addComponent(cmbEngine);
        textAreaLayout.addComponent(textTolerance);
        textAreaLayout.addComponent(textRuntimes);
        textAreaLayout.addComponent(cmbServer);
        
        HorizontalLayout optionLayout = new HorizontalLayout();
        optionLayout.addComponent(listTests);
        optionLayout.addComponent(textAreaLayout);
        optionLayout.setMargin(true);
        optionLayout.setSpacing(true);
        Panel confButtonPanel = new Panel();
        HorizontalLayout confButtonLayout = new HorizontalLayout();
        confButtonLayout.addComponent(btnAccept);
        confButtonLayout.addComponent(btnCancel);
        confButtonPanel.setContent(confButtonLayout);
        confButtonLayout.setMargin(true);
        confButtonLayout.setSpacing(true);
        VerticalLayout confWindowLayout = new VerticalLayout();
        confWindowLayout.addComponent(optionLayout);
        confWindowLayout.addComponent(confButtonPanel);
        confirmTests.setContent(confWindowLayout);
        
        this.updateList();
        
        confirmTests.center();
        
        return confirmTests;
	}
	
	public void updateData(HashSet<Object> rows) {
		markedRows.clear();
		markedRows.addAll(rows);
		//TODO: fntsservers!
		cmbServer.removeAllItems();
		String ping = "";
		for (Server server : ServerContainer.getServerContainer().getItemIds()) {
			ping = XmlRpcContact.ping(server.getIP());
			if (ping.matches("pong")) {
				cmbServer.addItem(server.getIP());
			}
		}
		
		this.updateList();
	}
	
	public void updateList() {
		listTests.setReadOnly(false);
		listTests.setValue("");
		for (Iterator i = markedRows.iterator(); i.hasNext();) {
			listTests.setValue(listTests.getValue() + i.next().toString() + "\n");
		}
		listTests.setReadOnly(true);
			
		//default values
		if (markedRows.size() == 1) {
			Script item = (Script) markedRows.iterator().next();
			textCaseName.setValue(item.getName());
		} else {
			textCaseName.setValue("Test Run");
		}
			
		try {
			cmbEngine.setValue(ServerContainer.getFirstServer().getEngines().get(0));
			textTolerance.setValue("80");
			textRuntimes.setValue("1");
			cmbServer.setValue(ServerContainer.getFirstServer().getIP());
		} catch (NullPointerException e) {
			//no servers to connect to, leaving the options empty
		}
	}
	
	public synchronized String[] getServerDetails(String server) {
    	Map<String, String> result = (HashMap<String, String>)XmlRpcContact.getServerDetails(server);
    	System.out.println(result.toString());
    	String[] engines = null; 
    	if (result.containsKey("engines")) {
    		engines = result.get("engines").split(" ");
    	} else {
    		String str = "errorextravaganza" + result.get("error");
    		engines = str.split("extravaganza");
    	}
    	return engines;
    }
	
}