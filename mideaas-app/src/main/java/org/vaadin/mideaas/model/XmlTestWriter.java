package org.vaadin.mideaas.model;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.vaadin.mideaas.test.ScriptContainer;
import org.vaadin.mideaas.test.Script;

import org.vaadin.mideaas.app.MideaasConfig;
import org.vaadin.mideaas.model.ServerContainer;
import org.vaadin.mideaas.model.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlTestWriter{
	
	public static synchronized void DOMWriteTestsToXml() {
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
				checked.appendChild(doc.createTextNode(String.valueOf(p.getCheck().getValue())));
				test.appendChild(checked);
				
				Element notes = doc.createElement("notes");
				notes.appendChild(doc.createTextNode(p.getNotes()));
				test.appendChild(notes);
				
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
				
				servers.appendChild(server);
			}
	 
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(MideaasConfig.getProjectsDir() + "test/" + "TestDataStorage.xml"));
	 
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
	
	public static synchronized void WriteTestsToXml() {
		XmlWriter xml = new XmlWriter();
		xml.startWriting();
	}
	
	public static synchronized String SAXloadTestsFromXml() {
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
  	      		boolean servers = false;
  	      		boolean server = false;
  	      		boolean serverengines = false;
  	      		Script scr;
  	      		Server serv;

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
	      			} else if (qName.equalsIgnoreCase("servers")) {
	      				servers = true;
	      			} else if (qName.equalsIgnoreCase("server")) {
	      				serv = new Server();
  	      				serv.setIP(attributes.getValue("IP").trim());
	      				server = true;
	      			} else if (qName.equalsIgnoreCase("engines")) {
	      				serverengines = true;
	      			}
  	      		}

  	      		public void endElement(String uri, String localName, String qName) throws SAXException {
  	      			if (qName.equalsIgnoreCase("project")) {
  	      				project = false;
  	      			} else if (qName.equalsIgnoreCase("tests")) {
  	      				tests = false;
  	      			} else if (qName.equalsIgnoreCase("test")) {
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
  	      			} else if (qName.equalsIgnoreCase("servers")) {
  	      				servers = false;
  	      			} else if (qName.equalsIgnoreCase("server")) {
  	      				ServerContainer.addServerObjectToContainer(serv);
  	      				server = false;
  	      			} else if (qName.equalsIgnoreCase("engines")) {
  	      				serverengines = false;
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
	      				scr.setNotes(new String(ch, start, length).trim());
	      			} else if (serverengines) {
	      				serv.setEngines(Arrays.asList(new String(ch, start, length).split(",")));
	      			}
  	      		}
  	      	};

  	      	File file = new File(MideaasConfig.getProjectsDir() + "test/" + "TestDataStorage.xml");	//TODO: needs changeable project name
  	      	InputStream inputStream= new FileInputStream(file);
  	      	Reader reader = new InputStreamReader(inputStream,"UTF-8");

  	      	InputSource is = new InputSource(reader);
  	      	is.setEncoding("UTF-8");

  	      	saxParser.parse(is, handler);
  	      	
  	      	return "ok";

		} catch (IOException e) {
			return "Something went wrong while loading file!";
		} catch (Exception e) {
  	      	e.printStackTrace();
  	      	return "Something went wrong while reading data!";
  	    }
	}
}