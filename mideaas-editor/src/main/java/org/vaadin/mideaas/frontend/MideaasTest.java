package org.vaadin.mideaas.frontend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vaadin.mideaas.tests.Script;
import org.vaadin.mideaas.tests.ScriptContainer;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
//import com.vaadin.terminal.ThemeResource;




public class MideaasTest extends CustomComponent {

    //Window subwindow;
    Window editwindow;

    Table table = new Table();

    HashSet<Object> markedRows = new HashSet<Object>();

    static final Action ACTION_MARK = new Action("Mark");
    static final Action ACTION_UNMARK = new Action("Unmark");
    static final Action ACTION_EDIT = new Action("Edit");
    static final Action ACTION_REMOVE = new Action("Remove");
    static final Action[] ACTIONS_UNMARKED = new Action[] { ACTION_MARK,
            ACTION_EDIT, ACTION_REMOVE };
    static final Action[] ACTIONS_MARKED = new Action[] { ACTION_UNMARK,
            ACTION_EDIT, ACTION_REMOVE };
    
    public ScriptContainer testContainer;
    
    private final com.vaadin.ui.TextArea editor;
    private static final String initialText = "Write script here";
    
    private String savemode = "add";

    
    public MideaasTest(String tabMessage) {
        
        
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
        
        textName.setWidth(null);
        //textLocation.setWidth(100, pc);
        //textDescription.setWidth(100, pc);
        
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
            		
            		table.setContainerDataSource(testContainer.addTestToContainer(testData));
            		UI.getCurrent().removeWindow(editwindow);
            	} else if (savemode == "edit") {
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
            		table.refreshRowCache();
            		UI.getCurrent().removeWindow(editwindow);
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
        table.setHeight("170px");
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
        
        table.setContainerDataSource(testContainer.createWithTestData());

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
                    markedRows.add(target);
                    table.refreshRowCache();
                } else if (ACTION_UNMARK == action) {
                    markedRows.remove(target);
                    table.refreshRowCache();
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
                } else {
                    selected.setValue("Selected: " + table.getValue());
                }
            }
        });        
        
        
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
                        	textName.setValue(" ");
            				textLocation.setValue(" ");
            				textDescription.setValue(" ");
            				editor.setValue("Write your script here");
                        }

                        // Center the window
                        editwindow.center();
                    }
                });        
        buttonLayout.addComponent(center);
        buttonLayout.addComponent(new Button("Run Marked Scripts"));
        buttonPanel.setContent(buttonLayout);
        mainLayout.addComponent(buttonPanel);

        mainLayout.addComponent(selected);
        mainPanel.setContent(mainLayout);
        setCompositionRoot(mainPanel);
        
        
    }
}
