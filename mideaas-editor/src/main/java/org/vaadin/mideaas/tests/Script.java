package org.vaadin.mideaas.tests;

import java.io.Serializable;
import com.vaadin.ui.CheckBox;


public class Script implements Serializable {
	// class to preserve test case data temporarily. The script itself should be saved in a file
	// given with git -variable, the rest of the data should be saved into a database(?)
    private String name = "";
    private String description = "";    
    private String result = "";    
    private String location = "";
    private boolean checked = false;
    
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
        this.result = result;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public boolean getCheck() {
        return checked;
    }
    public void setCheck(boolean checked) {
    	this.checked = checked;
    }
    
    @Override
    public String toString() {
    	String str = this.name + " in " + this.location;
    	return str;
    }

}
