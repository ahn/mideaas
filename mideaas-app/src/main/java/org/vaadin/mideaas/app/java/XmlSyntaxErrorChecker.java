package org.vaadin.mideaas.app.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.vaadin.mideaas.editor.ErrorChecker;
import org.xml.sax.SAXException;


public class XmlSyntaxErrorChecker implements ErrorChecker {
	
	
	@Override
	public List<Error> getErrors(String s) {
		// TODO: check errors!
		// disabled for now because seems to work strangely??
//		try {
//			parse(s);
//		} catch (ParserConfigurationException | SAXException | IOException e) {
//			return Collections.singletonList(new Error("error",0,0) /* TODO */);
//		}
		return Collections.emptyList();
	}
	
	@SuppressWarnings("unused")
	private void parse(String s) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder(); 
		builder.parse(new ByteArrayInputStream(s.getBytes()));
	}
}
