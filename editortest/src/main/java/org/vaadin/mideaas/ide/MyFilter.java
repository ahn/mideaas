package org.vaadin.mideaas.ide;

import java.util.HashSet;

import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;

public class MyFilter implements Filter {

	@Override
	public AceDoc filter(AceDoc doc) {
		// TODO Auto-generated method stub
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
