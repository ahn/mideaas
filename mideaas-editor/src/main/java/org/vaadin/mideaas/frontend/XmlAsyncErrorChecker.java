package org.vaadin.mideaas.frontend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.vaadin.aceeditor.client.Util;
import org.vaadin.mideaas.model.AsyncErrorChecker;
import org.vaadin.mideaas.model.ErrorChecker;
import org.vaadin.mideaas.model.ErrorChecker.Error;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlAsyncErrorChecker implements AsyncErrorChecker {

	@Override
	public void checkErrors(final String s, final ResultListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				listener.errorsChecked(getErrors(s));
			}
		}).start();
	}

	private static List<ErrorChecker.Error> getErrors(String s) {
		try {
			parse(s);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return Collections.singletonList(errorFromException(e, s));
		}

		return Collections.emptyList();
	}

	private static void parse(String s) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.parse(new ByteArrayInputStream(s.getBytes()));
	}

	private static Error errorFromException(Exception e, String s) {
		if (e instanceof SAXParseException) {
			SAXParseException se = (SAXParseException) e;
			int line = se.getLineNumber();
			int col = se.getColumnNumber();
			int pos = Util.cursorPosFromLineCol(s, line, col, 1);
			return new Error(se.getMessage(), pos, pos+1);
		} else {
			return new Error(e.getMessage(), 0, 0);
		}
	}
}
