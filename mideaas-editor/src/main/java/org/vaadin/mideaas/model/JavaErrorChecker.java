package org.vaadin.mideaas.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.vaadin.aceeditor.java.util.CompileResult;
import org.vaadin.aceeditor.java.util.CompilingService;
import org.vaadin.aceeditor.java.util.CompilingService.CompilationFinishedListener;
import org.vaadin.mideaas.model.ErrorChecker.Error;

public class JavaErrorChecker implements AsyncErrorChecker {
	
	private final String cls;
	private CompilingService compiler;

	public JavaErrorChecker(String fullClassName, CompilingService compiler) {
		cls = fullClassName;
		this.compiler = compiler;
	}

	private static List<Error> errorsFromDiagnostics(
			List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		ArrayList<Error> errs = new ArrayList<Error>(diagnostics.size());
		for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
			errs.add(new Error(d.getMessage(null), d.getStartPosition(), d.getEndPosition()));
		}
		return errs;
	}

	@Override
	public void checkErrors(final String s, final ResultListener listener) {
		compiler.compile(cls, s, new CompilationFinishedListener() {
			@Override
			public void compilationFinished(CompileResult result) {
				List<Error> errors;
				if (result.success) {
					errors = Collections.emptyList();
				}
				else {
					errors = errorsFromDiagnostics(result.diagnostics);
				}
				listener.errorsChecked(errors);
			}
		});
		
	}

}
