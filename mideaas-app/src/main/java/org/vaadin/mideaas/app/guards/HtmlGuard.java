package org.vaadin.mideaas.app.guards;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

public class HtmlGuard implements Guard {

	@Override
	public boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff) {
		return true;
	}

}
