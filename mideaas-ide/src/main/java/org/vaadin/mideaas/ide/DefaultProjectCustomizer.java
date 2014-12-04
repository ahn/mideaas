package org.vaadin.mideaas.ide;

import java.util.Map;

import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

public class DefaultProjectCustomizer implements ProjectCustomizer {

	
	@Override
	public IdeProject createProject(String id, String name, Map<String, String> files) {
		return new IdeProject(id, name);
	}
	
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
	public AsyncErrorChecker getErrorCheckerFor(String filename) {
		return null;
	}



}
