package org.vaadin.mideaas.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;

import com.steadystate.css.parser.CSSOMParser;

public class CSSGuard implements Guard {

	@Override
	public boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff) {
		System.out.println("css isAcceptable?");
		if (diff.getPatches().isEmpty()) {
			System.out.println("empty");
			return true;
		}
		else {
			System.out.println(diff);
		}
		
		try {
			return isAcceptable(candidate.getText());
		} catch (IOException e) {
			return false;
		}
	}

	private boolean isAcceptable(String text) throws IOException {
		
		final boolean[] errors = { false };
		
		InputStream stream = null;
		try {
			stream = new ByteArrayInputStream(text.getBytes("UTF-8"));
			InputSource source = new InputSource(new InputStreamReader(stream));
	        CSSOMParser parser = new CSSOMParser();
	        parser.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(CSSParseException arg0) throws CSSException {
					
				}
				
				@Override
				public void fatalError(CSSParseException arg0) throws CSSException {
					errors[0] = true;
				}
				
				@Override
				public void error(CSSParseException arg0) throws CSSException {
					errors[0] = true;
				}
			});
	        parser.parseStyleSheet(source, null, null);
		} catch (IOException e) {
			return false;
		}
		finally {
			if (stream != null) {
				stream.close();
			}
		}

		return !errors[0];

		
	}
}
