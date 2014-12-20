package org.vaadin.mideaas.app.checkers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.aceeditor.client.Util;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.ErrorChecker.Error;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;

import com.steadystate.css.parser.CSSOMParser;

public class CssErrorChecker implements AsyncErrorChecker {
	

	@Override
	public void checkErrors(final String s, final ResultListener listener) {
		try {
			listener.errorsChecked(getErrors(s));
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: must we call listener.errorChecked?
		}
	}
	
	private List<Error> getErrors(final String text) throws IOException {
		final LinkedList<Error> errors = new LinkedList<Error>();
		
		InputStream stream = null;
		try {
			stream = new ByteArrayInputStream(text.getBytes("UTF-8"));
			InputSource source = new InputSource(new InputStreamReader(stream));
			CSSOMParser parser = new CSSOMParser();
			parser.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(CSSParseException e) throws CSSException {
					
				}
				
				@Override
				public void fatalError(CSSParseException e) throws CSSException {
					errors.add(errorFromException(e, text));
				}
				
				@Override
				public void error(CSSParseException e) throws CSSException {
					errors.add(errorFromException(e, text));
				}
			});
			parser.parseStyleSheet(source, null, null);
		} catch (IOException e) {
			return errors;
		}
		finally {
			if (stream != null) {
				stream.close();
			}
		}
		return errors;
	}

	private static Error errorFromException(CSSParseException e, String text) {
		int line = e.getLineNumber();
		int col = e.getColumnNumber();
		int start = Util.cursorPosFromLineCol(text, line, col, 1);
		return new Error(e.getMessage(), start, start+1);
	}

}
