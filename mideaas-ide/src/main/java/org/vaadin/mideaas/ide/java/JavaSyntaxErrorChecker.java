package org.vaadin.mideaas.ide.java;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.TokenMgrError;
import japa.parser.ast.CompilationUnit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.vaadin.mideaas.editor.ErrorChecker;

public class JavaSyntaxErrorChecker implements ErrorChecker {

	@SuppressWarnings("unused")
	private CompilationUnit latestValid; // ???
	
	public JavaSyntaxErrorChecker() {
		
	}
	
	public JavaSyntaxErrorChecker(String initial) {
		try {
			latestValid = getCu(initial);
		} catch (ParseException e) {
			System.err.println("WARNING: initial java code has syntax errors.");
		}
	}
	
	@Override
	public List<Error> getErrors(String s) {
		try {
			getCu(s);
			return Collections.emptyList();
		} catch (ParseException | TokenMgrError e) {
			return Collections.singletonList(new Error("error",0,0) /* TODO */);
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
