package org.vaadin.mideaas.app.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vaadin.mideaas.app.VaadinProject;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class MideaasTestEditor extends CustomComponent {
	
	Window editwindow;
	
	private com.vaadin.ui.TextArea editor;
    private static final String initialText = "Write script here";
    
    final com.vaadin.ui.TextField textName = new com.vaadin.ui.TextField("Test name");
    final com.vaadin.ui.TextField textLocation = new com.vaadin.ui.TextField("Location");
    final com.vaadin.ui.TextField textDescription = new com.vaadin.ui.TextField("Description");
    final com.vaadin.ui.ComboBox cmbEngine = new com.vaadin.ui.ComboBox("Preferred engine");
    
    String savemode;
    Set<?> selection;
	
	public Window createEditor(String save, Set<?> newSelection, final MideaasTest mideaastest, final VaadinProject project) {
    	
		savemode = save;
		selection = newSelection;
		
		// Create the window
        editwindow = new Window("Edit script");
        editwindow.setWidth("650px");
        editwindow.setHeight("550px");
		
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
        
        try {
        	for (Server server : ServerContainer.getServerContainer().getItemIds()) {
        		for (String engine : server.getEngines()) {
        			if (!cmbEngine.containsId(engine)) {
        				cmbEngine.addItem(engine);
        			}
        		}
        	}
        } catch (NullPointerException e) {
        	//failed to find any servers
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
            					testData.add(cmbEngine.getValue());
            					testData.add(false);
            		
            					//write test into a file
            					try {
            						File path = new File(project.getProjectDir() + "/" + textLocation.getValue());
            						path.mkdirs();
            						File file = new File(path.getAbsolutePath() + "/" + textName.getValue() + ".txt");
            						System.out.println(file);
            						file.createNewFile();
            						
            						BufferedWriter out = new BufferedWriter(new FileWriter(file));
            						out.write(editor.getValue());
            						out.close();
            					} catch (Exception e) {
            						Notification.show("Whoops", "Writing to a file failed", Notification.Type.ERROR_MESSAGE);
            						e.printStackTrace();
            					}
            		
            					mideaastest.updateItemInTable(testData);
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
            					Script item = ScriptContainer.getScriptFromContainer((String)selection.iterator().next());
            					item.setName(textName.getValue());
            					item.setDescription(textDescription.getValue());
            					item.setLocation(textLocation.getValue());
            					item.setEngine((String)cmbEngine.getValue());
            		
            					//write test into a file
            					try {
            						System.out.println("testing");
            						File path = new File(project.getProjectDir() + "/" + textLocation.getValue());
            						path.mkdirs();
            						File file = new File(path.getAbsolutePath() + "/" + textName.getValue() + ".txt");
            						System.out.println(file);
            						file.createNewFile();
            						
            						BufferedWriter out = new BufferedWriter(new FileWriter(file));
            						out.write(editor.getValue());
            						out.close();
            					} catch (Exception e) {
            						Notification.show("Whoops", "Writing to a file failed", Notification.Type.ERROR_MESSAGE);
            						e.printStackTrace();
            					}
            		
            					//remove the old item and add a new one
            					ScriptContainer.addTestObjectToContainer(item);
            					mideaastest.updateTable();
            					
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
        editorButtonLayout.addComponent(saveButton);
        editorButtonLayout.addComponent(closeButton);        
        editorLayout.addComponent(editorButtonLayout);
        csslayout.addComponent(editorLayout);
        editwindow.setContent(editorLayout);
        
        return editwindow;
	}
	
	public Window editTest(String save, Set<?> newSelection, VaadinProject project) {
    		// Open the subwindow by adding it to the parent
			// window
    		if (editwindow.getParent() == null) {
    			try {
    				savemode = save;
    				
    				selection = newSelection;
    			
    				Script item = ScriptContainer.getScriptFromContainer((String)selection.iterator().next());
    				textName.setValue(item.getName());
    				textLocation.setValue(item.getLocation());
    				textDescription.setValue(item.getDescription());
    				cmbEngine.setValue(item.getEngine());
    				
    				String path = project.getProjectDir() + "/" + textLocation.getValue() + textName.getValue() + ".txt"; //TODO: project name needs to be dynamic
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