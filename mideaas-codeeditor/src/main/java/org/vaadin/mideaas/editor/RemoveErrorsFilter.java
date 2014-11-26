package org.vaadin.mideaas.editor;

import java.util.HashSet;

import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;

public class RemoveErrorsFilter implements Filter {

	@Override
	public AceDoc filter(AceDoc doc) {
		return docWithoutErrorMarkers(doc);
	}
	
	public static AceDoc docWithoutErrorMarkers(AceDoc doc) {
		HashSet<String> ems = new HashSet<String>();
		for (String m : doc.getMarkers().keySet()) {
			if (m.startsWith("error-")) {
				ems.add(m);
			}
		}
		return doc.withoutMarkers(ems);
	}

}