package org.vaadin.mideaas.app.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.mideaas.app.MideaasConfig;
import org.vaadin.mideaas.app.VaadinProject;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class MideaasTest extends CustomComponent {

    Window editwindow;
    
    private final VaadinProject project;

    public Table table = new Table();
    
    final MideaasTestEditor testeditor = new MideaasTestEditor();

    HashSet<Object> markedRows = new HashSet<Object>();

    static final Action ACTION_MARK = new Action("Mark");
    static final Action ACTION_UNMARK = new Action("Unmark");
    static final Action ACTION_EDIT = new Action("Edit");
    static final Action ACTION_REMOVE = new Action("Remove");
    static final Action[] ACTIONS_UNMARKED = new Action[] { ACTION_MARK,
            ACTION_EDIT, ACTION_REMOVE };
    static final Action[] ACTIONS_MARKED = new Action[] { ACTION_UNMARK,
            ACTION_EDIT, ACTION_REMOVE };
    
    private static final String initialText = "Write script here";
    
    private String savemode = "add";
    
    public MideaasTest(String tabMessage, final VaadinProject project) {
    	
    	this.project = project;
    	
    	final com.vaadin.ui.TextArea testNotes = new com.vaadin.ui.TextArea("Notes", "");
    	testNotes.setWidth("100%");
    	testNotes.setRows(15);
    	testNotes.setReadOnly(true);
        
        //final Label selected = new Label("No selection");
        final Label selected = new Label(project.getName());

        // A layout structure used for composition
        Panel mainPanel = new Panel();
        final VerticalLayout mainLayout = new VerticalLayout();       
        mainLayout.addComponent(table);
        
        // size
        table.setWidth("100%");
        table.setHeight("50%");
        // selectable
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);
        // turn on column reordering and collapsing
        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);

        table.addContainerProperty("check", java.lang.Boolean.class, null);
        table.addContainerProperty("name", String.class, null);
        table.addContainerProperty("description", String.class, null);
        table.addContainerProperty("result", String.class, null);
        
        table.setColumnWidth("check", 45);
        table.setColumnAlignment("check, ", Align.CENTER);
        table.setVisibleColumns(new Object[] { "check", "name", "description", "result" });
        
        // set column headers
        table.setColumnHeader("check", "Marked");
        table.setColumnHeader("name", "Test Name");
        table.setColumnHeader("description", "Description");
        table.setColumnHeader("result", "Result");
        
        table.setDescription("Available tests</br></br>" +
        		"Left click to select a test</br>" +
        		"Hold ctrl and left click to select multiple tests</br>" +
        		"Right click to bring up the action menu");
        
        //load and set data if possible
        String loadResult = XmlTestWriter.SAXloadTestsFromXml(project);
        if (loadResult.matches("ok")) {
        	for (Script p : ScriptContainer.getContainer().getItemIds()) {
        		table.addItem(new Object[] {p.getCheck(), p.getName(), p.getDescription(), p.getResult()}, p.getName());
        		if (p.getCheck() == true){
        			markedRows.add(p.getName());
        		}
        	}
        } else {
        	for (Script p : ScriptContainer.createWithTestData().getItemIds()) {
        		table.addItem(new Object[] {p.getCheck(), p.getName(), p.getDescription(), p.getResult()}, p.getName());
        	}
        	
        	// getting servers from the config file
            List<String> servers = Arrays.asList(MideaasConfig.getFNTSServers().split("\\s*,\\s*"));
            XmlRpcContact xmlrpc = new XmlRpcContact();
            String errServers = "";
            for (String server : servers) {
            	try {
            		Map<String, String> result = (HashMap<String, String>)xmlrpc.getServerDetails(server, "details");
            		ServerContainer.addServer(server, Arrays.asList(result.get("engines").split(" ")), result.get("details"), project);
            	} catch (Exception e) {
            		if (errServers.equals("")) {
            			errServers = server;
            		} else {
            			errServers = errServers + "\n" + server;
            		}
            	}
            }
            if (errServers != "") {
            	Notification.show("Whoops", "Unable to reach the following servers:\n" + errServers, Notification.Type.ERROR_MESSAGE);
            }
        }
        
        testeditor.createEditor("add", (Set<?>) table.getValue(), this, project);
        
        // Actions (a.k.a context menu)
        table.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (markedRows.contains((String)target)) {
                    return ACTIONS_MARKED;
                } else {
                    return ACTIONS_UNMARKED;
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (ACTION_MARK == action) {
                	Set<?> value = (Set<?>) table.getValue();
                	for (Object object : value) {
                		if (!markedRows.contains((String)object)) {
                			markedRows.add((String)object);
                			Script item = (Script)ScriptContainer.getScriptFromContainer((String)object);
                			item.setCheck(true);
                		}
                	}
                    markedRows.add((String)target);
                    System.out.println(markedRows);
                    ScriptContainer.updateScriptCheckValue(markedRows, project);
                    Set<?> selection = (Set<?>) table.getValue();
                    updateTable();
                    table.setValue(selection);
                    
                    
                } else if (ACTION_UNMARK == action) {
                    Set<?> value = (Set<?>) table.getValue();
                    for (Object object : value) {
                		if (markedRows.contains((String)object)) {
                			markedRows.remove((String)object);
                			Script item = (Script)ScriptContainer.getScriptFromContainer((String)object);
                			item.setCheck(false);
                		}
                	}
                    markedRows.remove((String)target);
                    System.out.println(markedRows);
                    ScriptContainer.updateScriptCheckValue(markedRows, project);
                    Set<?> selection = (Set<?>) table.getValue();
                    updateTable();
                    table.setValue(selection);
                    
                    
                } else if (ACTION_EDIT == action) {
                	// should open the window for editing
                	savemode = "edit";
                	Set<?> value = (Set<?>) table.getValue();
                	if (null == value || value.size() == 0) {
                		selected.setValue("Please select a test first");
                	} else if (value.size() > 1) {
                		selected.setValue("Please select only one test");
                	} else {
                		editwindow = testeditor.editTest(savemode, (Set<?>) table.getValue(), project);
                		UI.getCurrent().addWindow(editwindow);
                		// Center the window
                		editwindow.center();
                	}
                } else if (ACTION_REMOVE == action) {
                	Set<?> value = (Set<?>) table.getValue();
                	if (null == value || value.size() == 0) {
                		selected.setValue("Please select a test first");
                	} else {
                		//should remove the selected item from the table
                		for (Iterator i = value.iterator(); i.hasNext(); ) {
                			String strValue = (String)i.next();
                			removeItemFromTable(strValue);
                			table.removeItem(strValue);
                		}
                	}
                }
            }
        });        
        
        
        // listen for valueChange, a.k.a 'select' and update the label
        table.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                // in multiselect mode, a Set of itemIds is returned,
                // in singleselect mode the itemId is returned directly
                Set<?> value = (Set<?>) event.getProperty().getValue();
                if (null == value || value.size() == 0) {
                    selected.setValue("No selection");
                    testNotes.setReadOnly(false);
            		testNotes.setValue("");
            		testNotes.setReadOnly(true);
                } else {
                    selected.setValue("Selected: " + table.getValue());
                    if (value.size() == 1) {
                    	Script item = (Script) ScriptContainer.getScriptFromContainer((String)value.iterator().next());
                    	testNotes.setReadOnly(false);
                		testNotes.setValue(item.getNotes());
                		testNotes.setReadOnly(true);
                    } else {
                    	testNotes.setReadOnly(false);
                		testNotes.setValue("");
                		testNotes.setReadOnly(true);
                    }
                }
            }
        });        
        
        final TestRunConfirmation conf = new TestRunConfirmation();
        final Window confirmTests = conf.newWindow(markedRows, this, project);
        final Window settings = new XmlRpcServerDetails().newWindow(project);
        
        //buttons to the main window
        Panel buttonPanel = new Panel();
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button newScript = new Button("New Script",
                new Button.ClickListener() {
                    // inline click-listener
                    public void buttonClick(ClickEvent event) {
                    	if (!UI.getCurrent().getWindows().contains(editwindow)) {
                    		editwindow = testeditor.createTest();
                    		UI.getCurrent().addWindow(editwindow);
                    		// Center the window
                    		editwindow.center();
                    		
                    		//updateTable();
                    	} else {
                    		editwindow.focus();
                    	}
                    }
                });
        newScript.setDescription("Add a new test to the list");
        buttonLayout.addComponent(newScript);
        
        Button runScripts = new Button("Run Marked Scripts",
        	new Button.ClickListener() {
            	// inline click-listener
            	public void buttonClick(ClickEvent event) {
            		if (!UI.getCurrent().getWindows().contains(confirmTests)) {
            			conf.updateData(markedRows);
            			UI.getCurrent().addWindow(confirmTests);
            		} else {
            			// the window exists already
            			confirmTests.focus();
            		}
            	}
        });
        runScripts.setDescription("Change settings and run marked tests");
        buttonLayout.addComponent(runScripts);
        
        Button serverDetails = new Button("FNTS Server details",
            	new Button.ClickListener() {
                	// inline click-listener
                	public void buttonClick(ClickEvent event) {
                		if (!UI.getCurrent().getWindows().contains(settings)) {
                			UI.getCurrent().addWindow(settings);
                		} else{
                			// the window exists already
                			settings.focus();
                		}
                	}
            });
        serverDetails.setDescription("Add and inspect XMLRPC servers");
        buttonLayout.addComponent(serverDetails);
        
        buttonLayout.setMargin(true);
        buttonPanel.setContent(buttonLayout);
        mainLayout.addComponent(buttonPanel);

        Panel labelPanel = new Panel();
        Panel notePanel = new Panel();
        labelPanel.setContent(selected);
        mainLayout.addComponent(labelPanel);
        notePanel.setContent(testNotes);
        mainLayout.addComponent(notePanel);
        mainPanel.setContent(mainLayout);
        setCompositionRoot(mainPanel);
        
        
    }
    
    public synchronized void updateTable() {
    	table.removeAllItems();
    	for (Script p : ScriptContainer.getContainer().getItemIds()) {
    		table.addItem(new Object[] {p.getCheck(), p.getName(), p.getDescription(), p.getResult()}, p.getName());
    	}
    }
    
    public VaadinProject getProject() {
		return project;
	}
    
    public synchronized String[] getServerDetails(String server) {
    	XmlRpcContact xmlrpc = new XmlRpcContact();
    	Map<String, String> result = (HashMap<String, String>)xmlrpc.getServerDetails(server, "engines");
    	System.out.println(result.toString());
    	String[] engines = result.get("engines").split(" ");
    	return engines;
    }
    
    public synchronized void updateItemInTable(List testData) {
    	ScriptContainer.addTestToContainer(testData, project).getItemIds();
    	table.addItem(new Object[] {false, (String)testData.get(0), (String)testData.get(2), "NOT RUN"}, (String)testData.get(0));
    	
    }
    
    public synchronized void removeItemFromTable(String testname) {
		ScriptContainer.removeScriptFromContainer(testname, project);
    }
    
    public synchronized Set<?> getTableSelection() {
    	return (Set<?>) table.getValue();
    }

}
