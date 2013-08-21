package org.vaadin.mideaas.app;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.vaadin.mideaas.test.Script;
import org.vaadin.mideaas.test.ScriptContainer;
import org.vaadin.mideaas.tests.*;
import org.vaadin.mideaas.model.XmlRpcContact;
import org.vaadin.mideaas.app.XmlRpcServerDetails;

public class MideaasTest extends CustomComponent {

    Window editwindow;
    //Window confirmTests;

    // XXX should not be static?
    static public Table table = new Table();

    HashSet<Object> markedRows = new HashSet<Object>();

    static final Action ACTION_MARK = new Action("Mark");
    static final Action ACTION_UNMARK = new Action("Unmark");
    static final Action ACTION_EDIT = new Action("Edit");
    static final Action ACTION_REMOVE = new Action("Remove");
    static final Action[] ACTIONS_UNMARKED = new Action[] { ACTION_MARK,
            ACTION_EDIT, ACTION_REMOVE };
    static final Action[] ACTIONS_MARKED = new Action[] { ACTION_UNMARK,
            ACTION_EDIT, ACTION_REMOVE };
    
    private final com.vaadin.ui.TextArea editor;
    private static final String initialText = "Write script here";
    
    private String savemode = "add";

    private List<String> fntsServers = new ArrayList<String>();
    
    public MideaasTest(String tabMessage) {
        fntsServers.add("http://192.168.8.100:8000"); //TODO: this should be taken from a config file
        
    	final com.vaadin.ui.TextArea testNotes = new com.vaadin.ui.TextArea("Notes", "");
    	testNotes.setWidth("100%");
    	testNotes.setRows(15);
    	testNotes.setReadOnly(true);
        
        // Create the window
        editwindow = new Window("Edit script");
        //UI.getCurrent().addWindow(editwindow);
        // let's give it a size (optional)
        editwindow.setWidth("640px");
        editwindow.setHeight("480px");
        
        editor = new com.vaadin.ui.TextArea(null, initialText);
        editor.setRows(15);
        editor.setColumns(50);
        editor.setImmediate(true);
        
        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.setMargin(true);
        editorLayout.setSpacing(true);
        
        //add some content into the editor
        final com.vaadin.ui.TextField textName = new com.vaadin.ui.TextField("Test name");
        final com.vaadin.ui.TextField textLocation = new com.vaadin.ui.TextField("Location");
        final com.vaadin.ui.TextField textDescription = new com.vaadin.ui.TextField("Description");
        
        editorLayout.addComponent(textName);
        editorLayout.addComponent(textLocation);
        editorLayout.addComponent(textDescription);
        editorLayout.addComponent(editor);

        Button closeButton = new Button("Close", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) {
                // close the window by removing it from the parent window
            	UI.getCurrent().removeWindow(editwindow);
            }
        });
        
        Button saveButton = new Button("Save", new Button.ClickListener() {
            // inline click-listener
            public void buttonClick(ClickEvent event) {
                // should save the script into a file
            	if (savemode == "add") {
            		if (textName.getValue() != "") {
            			if (textLocation.getValue() != "") {
            				if (textDescription.getValue() != "") {
            					List testData = new ArrayList();
            					testData.add(textName.getValue());
            					testData.add(textLocation.getValue());
            					testData.add(textDescription.getValue());
            					testData.add(false);
            		
            					//write test into a file
            					try {
            						String path = textLocation.getValue() + textName.getValue() + ".txt";
            						BufferedWriter out = new BufferedWriter(new FileWriter(path));
            						out.write(editor.getValue());
            						out.close();
            					} catch (IOException e) {
            						Notification.show("Whoops", "Writing to a file failed", Notification.Type.ERROR_MESSAGE);
            						e.printStackTrace();
            					}
            		
            					table.setContainerDataSource(ScriptContainer.addTestToContainer(testData));
            					UI.getCurrent().removeWindow(editwindow);
            				} else {
                    			Notification.show("Whoops", "Description is required", Notification.Type.ERROR_MESSAGE);
                    		}
            			} else {
                			Notification.show("Whoops", "Location is required", Notification.Type.ERROR_MESSAGE);
                		}
            		} else {
            			Notification.show("Whoops", "Test name is required", Notification.Type.ERROR_MESSAGE);
            		}
            	} else if (savemode == "edit") {
            		if (textName.getValue() != "") {
            			if (textLocation.getValue() != "") {
            				if (textDescription.getValue() != "") {
            					//edit the selected item 
            					Set<?> value = (Set<?>) table.getValue();
            					Script item = (Script) value.iterator().next();
            					item.setName(textName.getValue());
            					item.setDescription(textDescription.getValue());
            					item.setLocation(textLocation.getValue());
            		
            					//write test into a file
            					try {
            						String path = textLocation.getValue() + textName.getValue() + ".txt";
            						BufferedWriter out = new BufferedWriter(new FileWriter(path));
            						out.write(editor.getValue());
            						out.close();
            					} catch (IOException e) {
            						Notification.show("Whoops", "Writing to a file failed", Notification.Type.ERROR_MESSAGE);
            						e.printStackTrace();
            					}
            		
            					//remove the old item and add a new one
            					table.removeItem(table.getValue());
            					table.addItem(item);
            					table.markAsDirty();
            					UI.getCurrent().removeWindow(editwindow);
            				} else {
                    			Notification.show("Whoops", "Description is required", Notification.Type.ERROR_MESSAGE);
                    		}
            			} else {
                			Notification.show("Whoops", "Location is required", Notification.Type.ERROR_MESSAGE);
                		}
            		} else {
            			Notification.show("Whoops", "Test name is required", Notification.Type.ERROR_MESSAGE);
            		}
            	} else {
            		//only two possible options, so this should not happen
            		Notification.show("Whoops", "We have a bad coder!", Notification.Type.WARNING_MESSAGE);
            	}
            }
        });
        
        HorizontalLayout editorButtonLayout = new HorizontalLayout();
        editorButtonLayout.addComponent(closeButton);
        editorButtonLayout.addComponent(saveButton);
        editorLayout.addComponent(editorButtonLayout);
        editwindow.setContent(editorLayout);
        
        
        
        final Label selected = new Label("No selection");        
        // A layout structure used for composition
        Panel mainPanel = new Panel();
        final VerticalLayout mainLayout = new VerticalLayout();       
        mainLayout.addComponent(table);

        // size
        table.setWidth("100%");
        table.setHeight("300px");
        // selectable
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);
        

        // set column headers
        table.addContainerProperty("Marked", CheckBox.class,  null);
        table.addContainerProperty("Script Name", String.class,  null);
        table.addContainerProperty("Location", String.class,  null);
        table.addContainerProperty("Description", String.class, null);
        table.addContainerProperty("Result", String.class, null);
        //table.setColumnHeaders(new String[] { "Name", "Location", "Description", "Result" });
        
        table.setContainerDataSource(ScriptContainer.createWithTestData());

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
                    System.out.println(markedRows);
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
                		// Open the subwindow by adding it to the parent
            			// window
                		if (editwindow.getParent() == null) {
                			try {
                				UI.getCurrent().addWindow(editwindow);
                				Script item = (Script) value.iterator().next();
                				textName.setValue(item.getName());
                				textLocation.setValue(item.getLocation());
                				textDescription.setValue(item.getDescription());
                				
                				String path = textLocation.getValue() + textName.getValue() + ".txt";
                				BufferedReader br = new BufferedReader(new FileReader(path));
                			    try {
                			        StringBuilder sb = new StringBuilder();
                			        String line = br.readLine();

                			        while (line != null) {
                			            sb.append(line);
                			            sb.append("\n");
                			            line = br.readLine();
                			        }
                			        editor.setValue(sb.toString());
                			    } catch (IOException e) {
                					Notification.show("Whoops", "Reading from a file failed", Notification.Type.ERROR_MESSAGE);
                    				e.printStackTrace();
                			    } finally {
                			        br.close();
                			    }
                			} catch (Exception e) {
                				e.printStackTrace();
                			}
                		}
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
        // turn on column reordering and collapsing
        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);
        
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
        final Window confirmTests = conf.newWindow(markedRows, fntsServers);
        final Window settings = new XmlRpcServerDetails().newWindow();
        
        //buttons to the main window
        Panel buttonPanel = new Panel();
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button center = new Button("New Script",
                new Button.ClickListener() {
                    // inline click-listener
                    public void buttonClick(ClickEvent event) {
                        if (editwindow.getParent() == null) {
                        	savemode = "add";
                            // Open the subwindow by adding it to the parent
                            // window
                        	UI.getCurrent().addWindow(editwindow);
                        	textName.setValue("");
            				textLocation.setValue("");
            				textDescription.setValue("");
            				editor.setValue("Write your script here");
                        }

                        // Center the window
                        editwindow.center();
                    }
                });
        buttonLayout.addComponent(center);
        buttonLayout.addComponent(new Button("Run Marked Scripts",
        	new Button.ClickListener() {
            	// inline click-listener
            	public void buttonClick(ClickEvent event) {
            		conf.updateData(markedRows, fntsServers);
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
}
