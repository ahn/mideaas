package org.vaadin.mideaas.model;

import java.io.*;

import org.vaadin.mideaas.app.MideaasConfig;
import org.vaadin.mideaas.test.Script;
import org.vaadin.mideaas.test.ScriptContainer;

public class XmlWriter {
    static int indentation = 0;
    static BufferedWriter out = null;

    public synchronized void startWriting() {
        try {
        	out = new BufferedWriter(new FileWriter(MideaasConfig.getProjectsDir() + "test/" + "TestDataStorage.xml")); //TODO: replace project name

        	this.childLoop(ScriptContainer.getContainer(), ServerContainer.getServerContainer());

        	out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
				out.close(); //trying to close the FileWriter in case it was left open
			} catch (IOException ioe) {
				// apparently it's closed already
			}
        }
    }     
    
    private void childLoop(ScriptContainer scriptcontainer, ServerContainer servercontainer) {
        this.startDocument();
        this.startElement("project", "name", "test"); //TODO: changeable project name
        this.startElement("tests", "", "");
        for (Script script : scriptcontainer.getItemIds()) {
        	this.startElement("test", "name", script.getName());
        	
        	this.startElement("location", "", "");
        	this.characters(script.getLocation());
        	this.endElement("location");
        	
        	this.startElement("description", "", "");
        	this.characters(script.getDescription());
        	this.endElement("description");
        	
        	this.startElement("location", "", "");
        	this.characters(script.getLocation());
        	this.endElement("location");
        	
        	this.startElement("result", "", "");
        	this.characters(script.getResult());
        	this.endElement("result");
        	
        	this.startElement("check", "", "");
        	//this.characters(String.valueOf(script.getCheck().getValue()));
        	this.characters(String.valueOf(script.getCheck()));
        	this.endElement("check");
        	
        	this.startElement("notes", "", "");
        	this.characters(script.getNotes());
        	this.endElement("notes");
        	
        	this.startElement("testengine", "", "");
        	this.characters(script.getEngine());
        	this.endElement("testengine");
        	
        	this.endElement("test");
        }
        this.endElement("tests");
        this.startElement("servers", "", "");
        for (Server server : servercontainer.getItemIds()) {
        	this.startElement("server", "IP", server.getIP());
        	
        	this.startElement("engines", "", "");
        	String engines = "";
        	for (String e : server.getEngines()) {
        		if (engines.isEmpty()) {
        			engines = e;
        		} else {
        			engines = engines + ", " + e;
        		}
        	}
        	this.characters(engines);
        	this.endElement("engines");
        	
        	this.startElement("details", "", "");
        	this.characters(server.getDetails());
        	this.endElement("details");
        	
        	this.endElement("server");
        }
        this.endElement("servers");
        this.endElement("project");
        
    }

    private void startDocument() {
    	try {
			out.write("<?xml version=\"1.0\" encoding=\"" + "UTF-8" + "\"?>");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private String getIndents() {
    	String indents = "";
    	for (int i = 0; i < indentation; i++) {
    		indents += "    ";
    	}
    	return indents;
    }

    private void startElement(String elementName, String attrName, String attrValue) {
    	try {
    		if (!attrName.isEmpty()) {
    			out.write("\n" + this.getIndents() + "<" + elementName + " " + attrName + "=\"" + attrValue + "\">");
    		} else {
    			out.write("\n" + this.getIndents() + "<" + elementName + ">");
    		}
    		indentation += 1;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private void characters(String elementContent) {
    	try {
			out.write(elementContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private void endElement(String elementName) {
    	indentation -= 1;
    	try {
    		if (elementName.matches("test") || elementName.matches("tests") || elementName.matches("server") || elementName.matches("servers") || elementName.matches("project")) {
    			out.write("\n" + this.getIndents() + "</" + elementName + ">");
    		} else {
    			out.write("</" + elementName + ">");
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}