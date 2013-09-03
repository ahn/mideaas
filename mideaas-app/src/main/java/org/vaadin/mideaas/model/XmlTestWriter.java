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
				checked.appendChild(doc.createTextNode(String.valueOf(p.getBoolCheck())));
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
	
	public static synchronized void SAXWriteTestsToXml() {
		
	}
	
	public static synchronized void loadTestsFromXml() {
		
	}

}