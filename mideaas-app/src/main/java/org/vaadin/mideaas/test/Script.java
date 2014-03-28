package org.vaadin.mideaas.test;

import java.io.Serializable;
import com.vaadin.ui.CheckBox;


public class Script implements Serializable {
	// class to preserve test case data temporarily. The script itself should be saved in a file
	// given with location -variable, the rest of the data should be saved into a database(?)
    private String name = "";
    private String description = "";    
    private String result = "";    
    private String location = "";
    private String engine = "";
    //private CheckBox checkbox = new CheckBox("");
    private boolean checkbox = false;
    private String notes = "";
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
    	if (result.matches("PASS") || result.matches("FAIL") || result.matches("BLOCKED") || result.matches("NOT RUN") || result.matches("RUNNING")) {
    		this.result = result;
    	} else {
    		result = "BLOCKED";
    	}
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getEngine() {
        return engine;
    }
    public void setEngine(String engine) {
        this.engine = engine;
    }
    //public CheckBox getCheck() {
    public boolean getCheck(){
    	//boolean checked = checkbox.getValue();
        return checkbox;
    }
    public void setCheck(boolean checked) {
    	//checkbox.setValue(checked);
    	checkbox = checked;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
    	return this.name;
    }

}
