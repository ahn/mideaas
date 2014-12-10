package org.vaadin.mideaas.app.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.vaadin.mideaas.app.VaadinProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlTestWriter{
	
	public static synchronized void DOMWriteTestsToXml(VaadinProject project) {
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("project");
			rootElement.setAttribute("name", "test");
			doc.appendChild(rootElement);
	 
			// upper level elements: tests and servers
			Element tests = doc.createElement("tests");
			rootElement.appendChild(tests);
			Element servers = doc.createElement("servers");
			rootElement.appendChild(servers);
	 
			for (Script p : ScriptContainer.getContainer().getItemIds()) {
				//subelement test
				Element test = doc.createElement("test");
				test.setAttribute("name", p.getName());
				
				Element description = doc.createElement("description");
				description.appendChild(doc.createTextNode(p.getDescription()));
				test.appendChild(description);
				
				Element result = doc.createElement("result");
				result.appendChild(doc.createTextNode(p.getResult()));
				test.appendChild(result);
				
				Element location = doc.createElement("location");
				location.appendChild(doc.createTextNode(p.getLocation()));
				test.appendChild(location);
				
				Element checked = doc.createElement("checked");
				checked.appendChild(doc.createTextNode(String.valueOf(p.getCheck())));
				test.appendChild(checked);
				
				Element notes = doc.createElement("notes");
				notes.appendChild(doc.createTextNode(p.getNotes()));
				test.appendChild(notes);
				
				Element testengine = doc.createElement("testengine");
				testengine.appendChild(doc.createTextNode(p.getEngine().trim()));
				test.appendChild(testengine);
				
				tests.appendChild(test);
			}
			
			for (Server s : ServerContainer.getServerContainer().getItemIds()) {
				//subelement test
				Element server = doc.createElement("server");
				server.setAttribute("IP", s.getIP());
				
				for (String eng : s.getEngines()) {
					Element engine = doc.createElement("engine");
					engine.appendChild(doc.createTextNode(eng));
					server.appendChild(engine);
				}
				
				Element details = doc.createElement("details");
				details.appendChild(doc.createTextNode(s.getDetails().trim()));
				server.appendChild(details);
				
				servers.appendChild(server);
			}
	 
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(project.getProjectDir() + "/" + "TestDataStorage.xml"));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void WriteTestsToXml(VaadinProject project) {
		XmlWriter xml = new XmlWriter();
		xml.startWriting(project);
	}
	
	public static synchronized String SAXloadTestsFromXml(VaadinProject project) {
		try {
			 
			SAXParserFactory factory = SAXParserFactory.newInstance();
  	      	SAXParser saxParser = factory.newSAXParser();

  	      	DefaultHandler handler = new DefaultHandler() {

  	      		boolean project = false;
  	      		boolean tests = false;
  	      		boolean test = false;
  	      		boolean testlocation = false;
  	      		boolean testdescription = false;
  	      		boolean testresult = false;
  	      		boolean testcheck = false;
  	      		boolean testnotes = false;
  	      		boolean testengine = false;
  	      		boolean servers = false;
  	      		boolean server = false;
  	      		boolean serverengines = false;
  	      		boolean serverdetails = false;
  	      		Script scr;
  	      		Server serv;
  	      		String notes = "";
  	      		String details = "";

  	      		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
  	      			if (qName.equalsIgnoreCase("project")) {
  	      				project = true;
  	      			} else if (qName.equalsIgnoreCase("tests")) {
  	      				tests = true;
  	      			} else if (qName.equalsIgnoreCase("test")) {
  	      				scr = new Script();
  	      				scr.setName(attributes.getValue("name").trim());
  	      				test = true;
  	      			} else if (qName.equalsIgnoreCase("location")) {
  	      				testlocation = true;
  	      			} else if (qName.equalsIgnoreCase("description")) {
	      				testdescription = true;
	      			} else if (qName.equalsIgnoreCase("result")) {
	      				testresult = true;
	      			} else if (qName.equalsIgnoreCase("check")) {
	      				testcheck = true;
	      			} else if (qName.equalsIgnoreCase("notes")) {
	      				testnotes = true;
	      			} else if (qName.equalsIgnoreCase("testengine")) {
	      				testengine = true;
	      			} else if (qName.equalsIgnoreCase("servers")) {
	      				servers = true;
	      			} else if (qName.equalsIgnoreCase("server")) {
	      				serv = new Server();
  	      				serv.setIP(attributes.getValue("IP").trim());
	      				server = true;
	      			} else if (qName.equalsIgnoreCase("engines")) {
	      				serverengines = true;
  	      			} else if (qName.equalsIgnoreCase("details")) {
  	      				serverdetails = true;
  	      			}
  	      		}

  	      		public void endElement(String uri, String localName, String qName) throws SAXException {
  	      			if (qName.equalsIgnoreCase("project")) {
  	      				project = false;
  	      			} else if (qName.equalsIgnoreCase("tests")) {
  	      				tests = false;
  	      			} else if (qName.equalsIgnoreCase("test")) {
  	      			    scr.setNotes(notes);
  	      			    notes = "";
  	      				ScriptContainer.addTestObjectToContainer(scr);
  	      				test = false;
  	      			} else if (qName.equalsIgnoreCase("location")) {
  	      				testlocation = false;
  	      			} else if (qName.equalsIgnoreCase("description")) {
  	      				testdescription = false;
  	      			} else if (qName.equalsIgnoreCase("result")) {
  	      				testresult = false;
  	      			} else if (qName.equalsIgnoreCase("check")) {
  	      				testcheck = false;
  	      			} else if (qName.equalsIgnoreCase("notes")) {
  	      				testnotes = false;
  	      			} else if (qName.equalsIgnoreCase("testengine")) {
	      				testengine = false;
  	      			} else if (qName.equalsIgnoreCase("servers")) {
  	      				servers = false;
  	      			} else if (qName.equalsIgnoreCase("server")) {
  	      				serv.setDetails(details);
  	      				ServerContainer.addServerObjectToContainer(serv);
  	      				server = false;
  	      			} else if (qName.equalsIgnoreCase("engines")) {
  	      				serverengines = false;
  	      			} else if (qName.equalsIgnoreCase("details")) {
	      				serverdetails = false;
	      			}
  	      		}

  	      		public void characters(char ch[], int start, int length) throws SAXException {
	      			if (testlocation) {
	      				scr.setLocation(new String(ch, start, length).trim());
	      			} else if (testdescription) {
	      				scr.setDescription(new String(ch, start, length).trim());
	      			} else if (testresult) {
	      				scr.setResult(new String(ch, start, length).trim());
	      			} else if (testcheck) {
	      				scr.setCheck(Boolean.valueOf(new String(ch, start, length).trim()));
	      			} else if (testnotes) {
	      				notes = notes + new String(ch, start, length).trim() + "\n";
	      			} else if (testengine) {
	      				scr.setEngine(new String(ch, start, length).trim());
	      			} else if (serverengines) {
	      				List<String> engines = Arrays.asList(new String(ch, start, length).split(","));
	      				List<String> trimmedEngines = new ArrayList<String>();
	      				for(String engine : engines){
	      					trimmedEngines.add(engine.trim());
	      				}
	      				serv.setEngines(trimmedEngines);
	      			} else if (serverdetails) {
	      				details = details + new String(ch, start, length).trim() + "\n";
	      			}
  	      		}
  	      	};

  	      	File file = new File(project.getProjectDir() + "/" + "TestDataStorage.xml");	//TODO: needs changeable project name
  	      	InputStream inputStream= new FileInputStream(file);
  	      	Reader reader = new InputStreamReader(inputStream,"UTF-8");

  	      	InputSource is = new InputSource(reader);
  	      	is.setEncoding("UTF-8");

  	      	saxParser.parse(is, handler);
  	      	
  	      	return "ok";

		} catch (IOException e) {
			//e.printStackTrace();
			return "Something went wrong while loading file!";
		} catch (Exception e) {
  	      	e.printStackTrace();
  	      	return "Something went wrong while reading data!";
  	    }
	}
}