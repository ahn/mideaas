package org.vaadin.mideaas.java;

import org.vaadin.aceeditor.Suggestion;

public class ImportingSuggestion extends Suggestion {

	private final String importClass;
	
	public ImportingSuggestion(String displayText, String descriptionText,
			String suggestionText, String importClass) {
		super(displayText, descriptionText, suggestionText);
		this.importClass = importClass;
	}

	public String getImportClass() {
		return importClass;
	}
}
