package org.vaadin.mideaas.editor;

import java.util.Collections;
import java.util.List;

/**
 * ErrorChecker that always returns 0 errors.
 */
public class NoErrorsErrorChecker implements ErrorChecker {
	@Override
	public List<Error> getErrors(String s) {
		return Collections.emptyList();
	}
}
