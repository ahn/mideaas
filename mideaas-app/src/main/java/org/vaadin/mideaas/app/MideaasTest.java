package org.vaadin.mideaas.app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.mideaas.model.ServerContainer;
import org.vaadin.mideaas.model.XmlRpcContact;
import org.vaadin.mideaas.model.XmlTestWriter;
import org.vaadin.mideaas.test.Script;
import org.vaadin.mideaas.test.ScriptContainer;

import org.vaadin.mideaas.app.MideaasTestEditor;

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
    //Window confirmTests;

    // XXX should not be static?
    static public Table table = new Table();
    
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
    
    //private final com.vaadin.ui.TextArea editor;
    private static final String initialText = "Write script here";
    
    private String savemode = "add";
    
    public MideaasTest(String tabMessage) {
    	
    	final com.vaadin.ui.TextArea testNotes = new com.vaadin.ui.TextArea("Notes", "");
    	testNotes.setWidth("100%");
    	testNotes.setRows(15);
    	testNotes.setReadOnly(true);
        
        final Label selected = new Label("No selection");        
        // A layout structure used for composition
        Panel mainPanel = new Panel();
        final VerticalLayout mainLayout = new VerticalLayout();       
        mainLayout.addComponent(table);

        // size
        table.setWidth("100%");
        table.setHeight("200px");
        // selectable
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);
        // turn on column reordering and collapsing
        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);

        table.addContainerProperty("check", com.vaadin.ui.CheckBox.class, null);
        table.addContainerProperty("name", String.class, null);
        table.addContainerProperty("description", String.class, null);
        table.addContainerProperty("notes", String.class, null);
        table.addContainerProperty("location", String.class, null);
        table.addContainerProperty("result", String.class, null);
        
        table.setColumnWidth("check", 45);
        table.setColumnAlignment("check, ", Align.CENTER);
        table.setColumnWidth("notes", 0);
        table.setColumnWidth("location", 0);
        table.setColumnCollapsed("notes", true);
        table.setColumnCollapsed("location", true);
        
        table.setVisibleColumns(new Object[] { "check", "name", "description", "result" });
        
        // set column headers
        table.setColumnHeader("check", "Marked");
        table.setColumnHeader("name", "Test Name");
        table.setColumnHeader("description", "Description");
        table.setColumnHeader("result", "Result");
        
        //load and set data if possible
        String loadResult = XmlTestWriter.SAXloadTestsFromXml();
        if (loadResult.matches("ok")) {
        	table.setContainerDataSource(ScriptContainer.getContainer());
        	for (Script p : ScriptContainer.getContainer().getItemIds()) {
        		System.out.println(p.getNotes());
        		if (p.getCheck().getValue() == true) {
        			markedRows.add(p);
        		}
        	}
        } else {
        	table.setContainerDataSource(ScriptContainer.createWithTestData());
        	
        	// getting servers from the config file
            List<String> servers = Arrays.asList(MideaasConfig.getFNTSServers().split("\\s*,\\s*"));
            XmlRpcContact xmlrpc = new XmlRpcContact();
            String errServers = "";
            for (String server : servers) {
            	try {
            		Map<String, String> result = (HashMap<String, String>)xmlrpc.getServerDetails(server);
            		ServerContainer.addServer(server, Arrays.asList(result.get("engines").split(" ")));
            	} catch (Exception e) {
            		if (errServers == "") {
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
        
        testeditor.createEditor("add");
        
        // Actions (a.k.a context menu)
        table.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (markedRows.contains(target)) {
                    return ACTIONS_MARKED;
                } else {
                    return ACTIONS_UNMARKED;
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (ACTION_MARK == action) {
                	Set<?> value = (Set<?>) table.getValue();
                	for (Object object : value) {
                		if (!markedRows.contains(object)) {
                			markedRows.add(object);
                			Script item = (Script)object;
                			item.setCheck(true);
                		}
                	}
                    //markedRows.add(target);
                    //System.out.println(markedRows);
                    /*for (Iterator i = value.iterator(); i.hasNext();) {
                    	Script item = (Script)i.next();
                    	item.setCheck(true);
                    }*/
                    table.markAsDirty();
                    
                } else if (ACTION_UNMARK == action) {
                    Set<?> value = (Set<?>) table.getValue();
                    for (Object object : value) {
                		if (markedRows.contains(object)) {
                			markedRows.remove(object);
                			Script item = (Script)object;
                			item.setCheck(false);
                		}
                	}
                    //markedRows.remove(target);
                    System.out.println(markedRows);
                    /*for (Iterator i = value.iterator(); i.hasNext();) {
                    	Script item = (Script)i.next();
                    	item.setCheck(false);
                    }*/
                    table.markAsDirty();
                    
                } else if (ACTION_EDIT == action) {
                	// should open the window for editing
                	// where is it saved?
                	savemode = "edit";
                	Set<?> value = (Set<?>) table.getValue();
                	if (null == value || value.size() == 0) {
                		selected.setValue("Please select a test first");
                	} else if (value.size() > 1) {
                		selected.setValue("Please select only one test");
                	} else {
                		editwindow = testeditor.editTest(savemode);
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
                			table.removeItem(i.next());
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
                    	Script item = (Script) value.iterator().next();
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
        final Window confirmTests = conf.newWindow(markedRows);
        final Window settings = new XmlRpcServerDetails().newWindow();
        
        //buttons to the main window
        Panel buttonPanel = new Panel();
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button center = new Button("New Script",
                new Button.ClickListener() {
                    // inline click-listener
                    public void buttonClick(ClickEvent event) {
                    	editwindow = testeditor.createTest();
                    	UI.getCurrent().addWindow(editwindow);
                    	// Center the window
                        editwindow.center();
                    }
                });
        buttonLayout.addComponent(center);
        buttonLayout.addComponent(new Button("Run Marked Scripts",
        	new Button.ClickListener() {
            	// inline click-listener
            	public void buttonClick(ClickEvent event) {
            		conf.updateData(markedRows);
            		UI.getCurrent().addWindow(confirmTests);
            	}
        }));
        buttonLayout.addComponent(new Button("FNTS Server details",
            	new Button.ClickListener() {
                	// inline click-listener
                	public void buttonClick(ClickEvent event) {
                		UI.getCurrent().addWindow(settings);
                	}
            }));
        buttonLayout.setMargin(true);
        buttonPanel.setContent(buttonLayout);
        mainLayout.addComponent(buttonPanel);

        mainLayout.addComponent(selected);
        mainLayout.addComponent(testNotes);
        mainPanel.setContent(mainLayout);
        setCompositionRoot(mainPanel);
        
        
    }
    
    public static synchronized void updateTable() {
    	table.setContainerDataSource(ScriptContainer.getContainer());
    	table.markAsDirty();
    }
    
    public synchronized String[] getServerDetails(String server) {
    	XmlRpcContact xmlrpc = new XmlRpcContact();
    	Map<String, String> result = (HashMap<String, String>)xmlrpc.getServerDetails(server);
    	System.out.println(result.toString());
    	String[] engines = result.get("engines").split(" ");
    	return engines;
    }
    
    public static synchronized void updateItemInTable(List testData) {
    	table.setContainerDataSource(ScriptContainer.addTestToContainer(testData));
    }
    
    public static synchronized void removeItemFromTable(Object item) {
    	table.removeItem(table.getValue());
		table.addItem(item);
		table.markAsDirty();
    }
    
    public static synchronized Set<?> getTableSelection() {
    	return (Set<?>) table.getValue();
    }
}
