package org.vaadin.mideaas.app.checkers;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.aceeditor.client.Util;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.ErrorChecker.Error;

import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;

public class JavaScriptErrorChecker implements AsyncErrorChecker {


	@Override
	public void checkErrors(final String s, final ResultListener listener) {
		listener.errorsChecked(getErrors(s));
	}

	public static List<Error> getErrors(String code) {
		Compiler compiler = new Compiler();

		CompilerOptions options = new CompilerOptions();

		// To get the complete set of externs, the logic in
		// CompilerRunner.getDefaultExterns() should be used here.
		SourceFile externs = SourceFile.fromCode("externs.js", "function alert(x) {}"); // TODO?

		// The dummy input name "input.js" is used here so that any warnings or
		// errors will cite line numbers in terms of input.js.
		SourceFile input = SourceFile.fromCode("input.js", code);

		compiler.setErrorManager(new BasicErrorManager() {

			@Override
			public void println(CheckLevel level, JSError error) {
				// Nothing...
			}

			@Override
			protected void printSummary() {
				// Nothing...
			}
		});

		// compile() returns a Result, but it is not needed here.
		compiler.compile(input, externs, options);

		LinkedList<Error> errors = new LinkedList<Error>();
		for (JSError e : compiler.getErrors()) {
			errors.add(errorFromJsError(e, code));
		}
		return errors;
	}

	private static Error errorFromJsError(JSError e, String text) {
		int line = e.getLineNumber();
		int col = e.getCharno();
		int start = Util.cursorPosFromLineCol(text, line, col, 1);
		return new Error(e.toString(), start, start+1);
	}

}
