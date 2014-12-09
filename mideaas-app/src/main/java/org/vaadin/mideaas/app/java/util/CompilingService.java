package org.vaadin.mideaas.app.java.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.vaadin.mideaas.app.VaadinProject;
import org.vaadin.mideaas.app.VaadinProject.ClasspathListener;

public class CompilingService implements ClasspathListener {

	public interface CompilationFinishedListener {
		public void compilationFinished(CompileResult result);
	}

	private final VaadinProject project;

	private final InMemoryCompiler compiler;

	private final ExecutorService pool = Executors.newSingleThreadExecutor();

	public CompilingService(VaadinProject project) {
		this.project = project;
		compiler = new InMemoryCompiler();
		refreshClasspath();
		project.addClasspathListener(this);
	}
	
	// XXX Wouldn't want to expose this...
	public InMemoryCompiler getInMemoryCompiler() {
		return compiler;
	}
	
	private void refreshClasspath() {
		String cp = project.getClassPath();
		if (cp!=null) {
			List<String> cpa = Arrays.asList(cp.split(File.pathSeparator));
			compiler.setClasspath(cpa);
		}
		else {
			System.err.println("WARNING: could not set project classpath");
		}
	}

	public void compile(final String fullJavaClassName,
			final String sourceCode, final CompilationFinishedListener listener) {
		pool.execute(new Runnable() {
			@Override
			public void run() {
				CompileResult result = compiler.compile(fullJavaClassName, sourceCode);
				if (result.success) {
					//mavenCompileFile(fullJavaClassName);
				}
				if (listener!=null) {
					listener.compilationFinished(result);
				}
			}
		});
	}

	@Override
	public void classpathChanged() {
		refreshClasspath();
	}

	public void compileAll(final Map<String, String> classes) {
		pool.execute(new Runnable() {
			@Override
			public void run() {
				compiler.compileAll(classes);
			}
		});
	}

//	public void compile(SharedView view) {
//		compile(view.getControllerFullName(), view.getControllerMud().getBaseText(), null);
//	}

	public void removeClass(String fullJavaClassName) {
		compiler.removeClass(fullJavaClassName);
	}

}
