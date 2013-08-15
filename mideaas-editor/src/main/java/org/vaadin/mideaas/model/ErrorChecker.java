package org.vaadin.mideaas.model;

import java.util.List;

public interface ErrorChecker {
	public class Error {
		public final String message;
		public final int start;
		public final int end;
		public Error(String message, long start, long end) {
			this.message = message;
			this.start = (int)start;
			this.end = (int)end;
		}
		
	}
	public List<Error> getErrors(String s);
}
