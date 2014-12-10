package org.vaadin.mideaas.app.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vaadin.mideaas.app.MideaasConfig;
import org.vaadin.mideaas.app.VaadinProject;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TestRunConfirmation extends Window {
	
	static final HashSet<Object> markedRows = new HashSet<Object>();
	static List<String> fntsServers = new ArrayList<String>();
	
	final Window confirmTests = new Window("Confirm runnable tests");
	
    final com.vaadin.ui.TextArea listTests = new com.vaadin.ui.TextArea();
	final com.vaadin.ui.TextField textCaseName = new com.vaadin.ui.TextField("Test run name");
	final com.vaadin.ui.TextField textTolerance = new com.vaadin.ui.TextField("Tolerance");
    final com.vaadin.ui.TextField textRuntimes = new com.vaadin.ui.TextField("Run # of times");
    final com.vaadin.ui.ComboBox cmbServer = new com.vaadin.ui.ComboBox("XMLRPC Server");
	
	protected Window newWindow(HashSet<Object> rows, final MideaasTest mideaasTest, final VaadinProject project){
		markedRows.addAll(rows);
		
		//the test confirmation window
        confirmTests.setWidth("640px");
        confirmTests.setHeight("390px");
        
        Panel labelPanel = new Panel();
        HorizontalLayout labelPanelLayout = new HorizontalLayout();
        Label label = new Label("This window contains the test run specific options and all the tests selected from the table. ");
        label.setContentMode(ContentMode.HTML);
        Label gap = new Label("&nbsp;");
        gap.setContentMode(ContentMode.HTML);
        gap.setWidth("15px");
        labelPanelLayout.addComponent(gap);
        labelPanelLayout.addComponent(label);
        labelPanel.setContent(labelPanelLayout);
        
        Panel p = new Panel("Selected tests");
        p.setContent(listTests);
        
        //fields that contain the info needed for running the tests
        listTests.setRows(10);
        listTests.setColumns(25);
        listTests.setReadOnly(false);
        listTests.setEnabled(true);
        
        
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
        } catch (NullPointerException e) {
        	//no servers to connect to, leaving the options empty
        	//cmbEngine.addItem("no engines available");
        	//cmbEngine.setEnabled(false);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
        listTests.setReadOnly(true);
        cmbServer.setImmediate(true);
        
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
						Script item = ScriptContainer.getScriptFromContainer((String)i.next());
						if (tests == "") {
							tests = item.getName();
						} else {
							tests = tests + ", " + item.getName();
						}
					}
			        	
					map.put("scriptNames", tests);
					System.out.println(tests);
					map.put("tolerance", (String)textTolerance.getValue());
					map.put("runtimes", (String)textRuntimes.getValue());
					
					XmlRpcContact.executeParallelTests((String)cmbServer.getValue(), map, MideaasConfig.getExecutorNumber(), mideaasTest, project);
					UI.getCurrent().removeWindow(confirmTests);
				}
			}
		});
        btnAccept.setDescription("Run selected tests using the selected XMLRPC Server");
        
        Button btnCancel = new Button("Cancel", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				// go back to main window
				UI.getCurrent().removeWindow(confirmTests);
			}
		});
        
        //create the confirmation window layout
        VerticalLayout textAreaLayout = new VerticalLayout();
        textAreaLayout.addComponent(textCaseName);
        textAreaLayout.addComponent(textTolerance);
        textAreaLayout.addComponent(textRuntimes);
        textAreaLayout.addComponent(cmbServer);
        
        //descriptions
        textCaseName.setDescription("Test run name is used for sorting the tests and in reports by some engines");
        textTolerance.setDescription("Tolerance tells how many percent of the tests must pass in order to the test run to pass");
        textRuntimes.setDescription("How many times all tests need to be run");
        cmbServer.setDescription("The server where the tests will be run");
        listTests.setDescription("List of tests selected for this test run");
        
        HorizontalLayout optionLayout = new HorizontalLayout();
        optionLayout.addComponent(p);
        optionLayout.addComponent(textAreaLayout);
        optionLayout.setMargin(true);
        optionLayout.setSpacing(true);
        Panel optionPanel = new Panel();
        optionPanel.setContent(optionLayout);
        Panel confButtonPanel = new Panel();
        HorizontalLayout confButtonLayout = new HorizontalLayout();
        confButtonLayout.addComponent(btnAccept);
        confButtonLayout.addComponent(btnCancel);
        confButtonPanel.setContent(confButtonLayout);
        confButtonLayout.setMargin(true);
        confButtonLayout.setSpacing(true);
        VerticalLayout confWindowLayout = new VerticalLayout();
        confWindowLayout.addComponent(labelPanel);
        confWindowLayout.addComponent(optionPanel);
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
		try {
			cmbServer.removeAllItems();
			String ping = "";
			for (Server server : ServerContainer.getServerContainer().getItemIds()) {
				ping = XmlRpcContact.ping(server.getIP());
				if (ping.matches("pong")) {
					cmbServer.addItem(server.getIP());
				}
			}
		} catch (NullPointerException e) {
			//no servers were found 
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
			Script item = ScriptContainer.getScriptFromContainer((String) markedRows.iterator().next());
			textCaseName.setValue(item.getName());
		} else {
			textCaseName.setValue("Test Run");
		}
			
		try {
			textTolerance.setValue("80");
			textRuntimes.setValue("1");
			cmbServer.setValue(ServerContainer.getFirstServer().getIP());
		} catch (NullPointerException e) {
			//no servers to connect to, leaving the options empty
		}
	}
	
	public synchronized String[] getServerDetails(String server) {
    	Map<String, String> result = (HashMap<String, String>)XmlRpcContact.getServerDetails(server, "engines");
    	System.out.println(result.toString());
    	String[] engines = null; 
    	if (result.containsKey("engines")) {
    		engines = result.get("engines").split(" ");
    	} else {
    		String str = "errorextravaganza" + result.get("error");
    		engines = str.split("extravaganza");	//if the error message contains this word, I'll eat my hat
    	}
    	return engines;
    }
	
}