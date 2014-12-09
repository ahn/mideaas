package org.vaadin.mideaas.ide;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

public class DefaultProjectCustomizer implements IdeProjectCustomizer {

	
	@Override
	public Guard getUpwardsGuardFor(String fileName) {
		return null;
	}

	@Override
	public Guard getDownwardsGuardFor(String filename) {
		return null;
	}

	@Override
	public Filter getFilterFor(String filename) {
		return null;
	}

	@Override
	public AsyncErrorChecker getErrorCheckerFor(String filename, IdeProject project) {
		return null;
	}



}
