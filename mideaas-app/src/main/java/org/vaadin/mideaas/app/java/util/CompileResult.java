package org.vaadin.mideaas.app.java.util;

import java.util.Collections;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompileResult {
	public final boolean success;
	public final List<Diagnostic<? extends JavaFileObject>> diagnostics;
	
	public static final CompileResult SUCCESS = new CompileResult(true);
	public static final CompileResult FAILURE = new CompileResult(false);
	
	public static CompileResult FAILURE_WITH_DIAGNOSTICS(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		return new CompileResult(false, diagnostics);
	}
	
	private CompileResult(boolean success,
			List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		this.success = success;
		this.diagnostics = diagnostics;
	}
	
	private CompileResult(boolean success) {
		this.success = success;
		this.diagnostics = Collections.emptyList();
	}

}
