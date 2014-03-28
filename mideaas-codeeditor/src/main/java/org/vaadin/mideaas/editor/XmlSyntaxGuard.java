package org.vaadin.mideaas.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.xml.sax.SAXException;

public class XmlSyntaxGuard implements Guard {
	
	@Override
	public boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff) {
		try {
			parse(candidate.getText());
		} catch (ParserConfigurationException | SAXException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			// TODO ???
			return false;
		}
		return true;
	}
	
	private void parse(String s) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder(); 
		builder.parse(new ByteArrayInputStream(s.getBytes()));
	}
	
}
