package org.vaadin.mideaas.app.guards;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.TokenMgrError;
import japa.parser.ast.CompilationUnit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

public class JavaGuard implements Guard {
	
	@Override
	public boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff) {
		try {
			getCu(candidate.getText());
			return true;
		} catch (ParseException | TokenMgrError e) {
			return false;
		}
	}
	
	private static CompilationUnit getCu(InputStream is) throws ParseException {
		try {
			return JavaParser.parse(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static CompilationUnit getCu(String code) throws ParseException {
		return getCu(new ByteArrayInputStream(code.getBytes()));
	}

	
}
