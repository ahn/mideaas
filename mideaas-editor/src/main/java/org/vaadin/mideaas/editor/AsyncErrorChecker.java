package org.vaadin.mideaas.editor;

import java.util.List;


public interface AsyncErrorChecker {
	public interface ResultListener {
		public void errorsChecked(List<ErrorChecker.Error> errors);
	}
	public void checkErrors(String s, ResultListener listener);
}
