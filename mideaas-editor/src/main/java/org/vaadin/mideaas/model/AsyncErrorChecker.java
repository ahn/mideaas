package org.vaadin.mideaas.model;

import java.util.List;

import org.vaadin.mideaas.model.ErrorChecker.Error;

public interface AsyncErrorChecker {
	public interface ResultListener {
		public void errorsChecked(List<Error> errors);
	}
	public void checkErrors(String s, ResultListener listener);
}
