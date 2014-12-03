package org.vaadin.mideaas.ide;

import java.util.HashMap;

import org.vaadin.aceeditor.TextRange;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class ClaraXmlHandler extends DefaultHandler {

	private static final String URN_IMPORT = "urn:import:";
	private Locator locator;
	private int curLine;
	private int curCol;
	private boolean foundElementUnderCursor = false;
	public String currId;
	public String currCls;
	private HashMap<String, String> imports = new HashMap<String, String>();
	String rootCls;

	public ClaraXmlHandler(TextRange selection) {
		curLine = selection.getEndRow() + 1;
		curCol = selection.getEndCol();
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		if (uri.startsWith(URN_IMPORT)) {
			imports.put(prefix, uri.substring(URN_IMPORT.length()));
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (foundElementUnderCursor && rootCls != null) {
			// Nothing to do if both cursor element and root found.
			return;
		}

		int line = locator.getLineNumber();
		int col = locator.getColumnNumber();

		if (line > curLine || (line == curLine && col > curCol)) {
			foundElementUnderCursor = true;
		}

		if (uri.startsWith(URN_IMPORT)) {
			currCls = uri.substring(URN_IMPORT.length()) + "." + localName;
		} else {
			currCls = localName;
		}

		if (rootCls == null) {
			rootCls = currCls;
		}

		currId = attributes.getValue("id");
	}

}