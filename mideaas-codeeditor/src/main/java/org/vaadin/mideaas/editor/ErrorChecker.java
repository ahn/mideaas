package org.vaadin.mideaas.editor;

import java.util.List;

public interface ErrorChecker {
	public class Error {
		public final String message;
		public final int start;
		public final int end;
		public Error(String message, int start, int end) {
			this.message = message;
			this.start = start;
			this.end = end;
		}
		
	}
	public List<Error> getErrors(String s);
}
