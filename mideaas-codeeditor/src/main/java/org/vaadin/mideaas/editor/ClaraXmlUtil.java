package org.vaadin.mideaas.editor;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClaraXmlUtil {
	
	public static String createHelloWorld(String layoutClass, String message) {
		
		try {
			Document doc = createEmptyDoc();
			Element root = doc.createElement(layoutClass);
			root.setAttribute("xmlns", "urn:import:com.vaadin.ui");
			root.setAttribute("id", "layout1");
			//root.setAttribute("xmlns:tk", "urn:import:com.vaadin.addon.touchkit.ui");
			doc.appendChild(root);
			Element label = doc.createElement("Label");
			label.setAttribute("id", "label1");
			label.setAttribute("value", message);
			root.appendChild(label);
			Element button = doc.createElement("Button");
			button.setAttribute("id", "button1");
			button.setAttribute("caption", "Click here");
			root.appendChild(button);
			return docToString(doc);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; // XXX
	}
	
	private static Document createEmptyDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document doc = docFactory.newDocumentBuilder().newDocument();
        doc.setXmlVersion("1.0");
        return doc;
    }
	
    private static String docToString(Document doc) throws TransformerException {
        DOMSource source = new DOMSource(doc);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(stream);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(source, result);
        return stream.toString();
    }
}
