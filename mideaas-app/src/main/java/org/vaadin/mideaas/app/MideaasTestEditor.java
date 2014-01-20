package org.vaadin.mideaas.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

import org.vaadin.mideaas.model.Server;
import org.vaadin.mideaas.model.ServerContainer;
import org.vaadin.mideaas.test.Script;
import org.vaadin.mideaas.test.ScriptContainer;


import org.vaadin.mideaas.app.MideaasTest;

public class MideaasTestEditor extends CustomComponent {
	
	Window editwindow;
	
	private com.vaadin.ui.TextArea editor;
    private static final String initialText = "Write script here";
    
    final com.vaadin.ui.TextField textName = new com.vaadin.ui.TextField("Test name");
    final com.vaadin.ui.TextField textLocation = new com.vaadin.ui.TextField("Location");
    final com.vaadin.ui.TextField textDescription = new com.vaadin.ui.TextField("Description");
    final com.vaadin.ui.ComboBox cmbEngine = new com.vaadin.ui.ComboBox("Preferred engine");
    
    String savemode;
	
	public Window createEditor(String save) {
    	
		savemode = save;
		
		// Create the window
        editwindow = new Window("Edit script");
        // let's give it a size (optional)
        editwindow.setWidth("640px");
        editwindow.setHeight("530px");
		
    	final com.vaadin.ui.TextArea testNotes = new com.vaadin.ui.TextArea("Notes", "");
    	testNotes.setWidth("100%");
    	testNotes.setRows(15);
    	testNotes.setReadOnly(true);
        
        editor = new com.vaadin.ui.TextArea(null, initialText);
        editor.setRows(15);
        editor.setColumns(50);
        editor.setImmediate(true);
        
        CssLayout csslayout = new CssLayout();
        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.setMargin(true);
        editorLayout.setSpacing(true);
        
        textName.setRequired(true);
        textLocation.setRequired(true);
        textDescription.setRequired(true);
        
        //System.out.println("Servers: " + ServerContainer.getServerContainer().getItemIds());
        for (Server server : ServerContainer.getServerContainer().getItemIds()) {
        	for (String engine : server.getEngines()) {
        		if (!cmbEngine.containsId(engine)) {
        			cmbEngine.addItem(engine);
        		}
        	}
        }
        
        editorLayout.addComponent(textName);
        editorLayout.addComponent(textLocation);
        editorLayout.addComponent(textDescription);
        editorLayout.addComponent(cmbEngine);
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
            						File path = new File(MideaasConfig.getProjectsDir() + "test/" + textLocation.getValue() + textName.getValue() + ".txt"); //TODO: project name needs to be dynamic
            						BufferedWriter out = new BufferedWriter(new FileWriter(path));
            						out.write(editor.getValue());
            						out.close();
            					} catch (IOException e) {
            						Notification.show("Whoops", "Writing to a file failed", Notification.Type.ERROR_MESSAGE);
            						e.printStackTrace();
            					}
            		
            					MideaasTest.updateItemInTable(testData);
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
            					Set<?> value = MideaasTest.getTableSelection();
            					Script item = (Script) value.iterator().next();
            					item.setName(textName.getValue());
            					item.setDescription(textDescription.getValue());
            					item.setLocation(textLocation.getValue());
            					item.setEngine((String)cmbEngine.getValue());
            		
            					//write test into a file
            					try {
            						String path = MideaasConfig.getProjectsDir() + "test/" + textLocation.getValue() + textName.getValue() + ".txt"; //TODO: project name needs to be dynamic
            						BufferedWriter out = new BufferedWriter(new FileWriter(path));
            						out.write(editor.getValue());
            						out.close();
            					} catch (IOException e) {
            						Notification.show("Whoops", "Writing to a file failed", Notification.Type.ERROR_MESSAGE);
            						e.printStackTrace();
            					}
            		
            					//remove the old item and add a new one
            					MideaasTest.removeItemFromTable(item);
            					ScriptContainer.addTestObjectToContainer(item);
            					MideaasTest.updateTable();
            					
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
            		Notification.show("Whoops", "We have a bad coder!", Notification.Type.ERROR_MESSAGE);
            	}
            	
            }
        });
        
        HorizontalLayout editorButtonLayout = new HorizontalLayout();
        editorButtonLayout.addComponent(closeButton);
        editorButtonLayout.addComponent(saveButton);
        editorLayout.addComponent(editorButtonLayout);
        csslayout.addComponent(editorLayout);
        editwindow.setContent(editorLayout);
        
        return editwindow;
	}
	
	public Window editTest(String save) {
    		// Open the subwindow by adding it to the parent
			// window
    		if (editwindow.getParent() == null) {
    			try {
    				savemode = save;
    				
    				Set<?> value = MideaasTest.getTableSelection();
    			
    				Script item = (Script) value.iterator().next();
    				textName.setValue(item.getName());
    				textLocation.setValue(item.getLocation());
    				textDescription.setValue(item.getDescription());
    				cmbEngine.setValue(item.getEngine());
    				
    				String path = MideaasConfig.getProjectsDir() + "test/" + textLocation.getValue() + textName.getValue() + ".txt"; //TODO: project name needs to be dynamic
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
    		return editwindow;
    	
	}
	
	public Window createTest() {
		if (editwindow.getParent() == null) {
        	savemode = "add";
        	textName.setValue("");
			textLocation.setValue("");
			textDescription.setValue("");
			editor.setValue("Write your script here");
			
			List<String> engines = (List<String>) cmbEngine.getItemIds();
			cmbEngine.setValue(engines.get(0));
        }
		return editwindow;
	}
	
}