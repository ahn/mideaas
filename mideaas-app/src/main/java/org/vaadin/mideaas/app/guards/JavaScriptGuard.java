package org.vaadin.mideaas.app.guards;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
public class JavaScriptGuard implements Guard {

	@Override
	public boolean isAcceptable(AceDoc candidate, ServerSideDocDiff diff) {
		return compile(candidate.getText());
	}
	
	public static boolean compile(String code) {
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
			}});
	    
	    //SourceFile s = new com.google.javascript.jscomp.SourceFile("asdas", "asdaop");
	    // compile() returns a Result, but it is not needed here.
	    compiler.compile(input, externs, options);

	    // The compiler is responsible for generating the compiled code; it is not
	    // accessible via the Result.
	    return compiler.getErrorCount() == 0;
	  }

}
