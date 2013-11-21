package org.vaadin.mideaas.java.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;


//made some copy-pasting: http://www.velocityreviews.com/forums/t643874-re-run-time-compilation.html
public class InMemoryCompiler {

	private URLClassLoader ucl;
	private LinkedList<URL> jarUrls;
	private String classPath = null;
	private ClassInfoStorage classInfo;
	
	private SpecialClassLoader xcl;
	private HashMap<String, JavaFileObject> sources = new HashMap<String, JavaFileObject>();
	
	public InMemoryCompiler() {
		setClasspath(new LinkedList<String>());
	}
	
	synchronized public void setClasspath(List<String> paths) {
		resetClasspath();
		for (String p : paths) {
			addToClasspath(p);
			classInfo.addClasspathItem(p);
		}
		URL[] array = jarUrls.toArray(new URL[jarUrls.size()]);
		ucl = new URLClassLoader(array);
		xcl = new SpecialClassLoader(ucl);
	}
	
	private void resetClasspath() {
		classInfo = new ClassInfoStorage();
		for (String s : sources.keySet()) {
			classInfo.addClass(s);
		}
		classInfo.loadClassesFromClasspathFile("java-classes.txt");
		jarUrls = new LinkedList<URL>();
		classPath = null;
	}
	
	private void addToClasspath(String path) {
		if (classPath==null) {
			classPath = path;
		}
		else {
			classPath += System.getProperty("path.separator") + path;
		}
		try {
			jarUrls.add(new URL("file:///" + path));
			
		} catch (MalformedURLException e) {
			System.err.println("WARNING: skipping classpath item: " + path);
		}
	}
	
	public CompilationTask getCompilationTask(
			String fullJavaClassName, String sourceCode) {
		return getCompilationTask(fullJavaClassName, sourceCode, null, false);
	}
	
	synchronized public CompilationTask getCompilationTask(
			String fullJavaClassName, String sourceCode,
			DiagnosticCollector<JavaFileObject> collector,
			boolean includeOthers) {
		
		JavaFileObject so = new MemorySource(fullJavaClassName, sourceCode);
		
		Collection<JavaFileObject> compilationUnits;
		if (includeOthers) {
			compilationUnits = new LinkedList<JavaFileObject>();
			compilationUnits.add(so);
			for (Entry<String, JavaFileObject> e : sources.entrySet()) {
				if (!e.getKey().equals(fullJavaClassName)) {
					compilationUnits.add(e.getValue());
				}
			}
		}
		else {
			compilationUnits = Collections.singletonList(so);
		}

		List<String> options = new ArrayList<String>();
		
		if (classPath != null) {
			List<String> classPathOpt = Arrays.asList("-classpath", classPath);
			options.addAll(classPathOpt);
		}
		
		// -proc:none means that compilation takes place without annotation processing
		options.add("-proc:none");

		// compiles the thing
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		// Must create a new classloader every time because its impossible
		// to update a class in the same classloader.
		xcl = new SpecialClassLoader(ucl, xcl);

		SpecialJavaFileManager specu = new SpecialJavaFileManager(compiler.getStandardFileManager(collector, null, null), xcl);
//		SpecialJavaFileManager specu = new SpecialJavaFileManager(compiler.getStandardFileManager(collector, null, null), ucl);
		
		CompilationTask task = compiler.getTask(null, specu, collector,
				options, null, compilationUnits);
		
		return task;
	}

	// Compiles the thing
	synchronized public CompileResult compile(String fullJavaClassName, String sourceCode) {
		
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
		CompilationTask task = getCompilationTask(fullJavaClassName, sourceCode, collector, true /* XXX*/);
		if (task == null) {
			return CompileResult.FAILURE;
		}
		boolean success = task.call();
		if (success) {
			saveSuccesful(fullJavaClassName, sourceCode);
			return CompileResult.SUCCESS;
		}
		else {
			List<Diagnostic<? extends JavaFileObject>> ofThis = errorsInThisClass(fullJavaClassName, collector.getDiagnostics());
			if (ofThis.isEmpty()) {
				saveSuccesful(fullJavaClassName, sourceCode);
				return CompileResult.SUCCESS;
			}
			return CompileResult.FAILURE_WITH_DIAGNOSTICS(collector.getDiagnostics());
		}
	}
	
	private void saveSuccesful(String fullJavaClassName, String sourceCode) {
		sources.put(fullJavaClassName, new MemorySource(fullJavaClassName, sourceCode));
		sources.put(fullJavaClassName, new MemorySource(fullJavaClassName, sourceCode));
		classInfo.addClass(fullJavaClassName);
	}
	
	private List<Diagnostic<? extends JavaFileObject>> errorsInThisClass(String className,
			List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		String name = "/"+className.replace('.', '/')+".java";
		List<Diagnostic<? extends JavaFileObject>> ofThis = new LinkedList<Diagnostic<? extends JavaFileObject>>();
		for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
			JavaFileObject src = d.getSource();
			if (src!=null && name.equals(src.getName())) {
				ofThis.add(d);
			}
			
		}
		return ofThis;
	}

	
	synchronized public void removeClass(String fullJavaClassName) {
		sources.remove(fullJavaClassName);
		xcl.removeClass(fullJavaClassName);
		classInfo.removeClass(fullJavaClassName);
	}
	
	// gets class based on name and imports
	// TODO: this could be done more nicely
	synchronized public Class<?> getClassByName(String className, String currentPackage, List<String> imports) throws ClassNotFoundException {
		// if classname contains dots, then it is propably somethin like
		// com.thing.classname
		// and we do not need to go thru the imports
		if (className.contains(".")) {
			// tries to find class directly
			return this.loadClassPrivate(className);
		} else {
			// goes thru imports and triest to find the right one or the one
			// with the *
			String found = null;
			List<String> importPaths = new ArrayList<String>();
			importPaths.add(currentPackage);
			// by default we always check form java.lang
			importPaths.add("java.lang");
			for (String importName : imports) {
				if (importName.contains(className)) {
					found = importName;
				} else if (importName.endsWith(".*")) {
					importPaths.add(importName.substring(0,
							importName.indexOf(".*")));
				}
			}
			if (found!=null) {
				return loadClassTryDollar(found);
			}
			for (String i : importPaths) {
				// iterates thru all the ".*" paths and tries to find the
				// right one
				String testThis = i + "." + className;
				try {
					return loadClassPrivate(testThis);
				}
				catch(ClassNotFoundException e) {
					// continue trying...
				}
			}
			
		}
		
		System.err.println("Could not find class: " + className);
		throw new ClassNotFoundException(className);
	}

	private Class<?> loadClassPrivate(String name) throws ClassNotFoundException {
		return xcl.loadClass(name);
	}
	
	synchronized public Class<?> loadClassTryDollar(String name) throws ClassNotFoundException {
		try {
			return loadClassPrivate(name);
		}
		catch (ClassNotFoundException e) {
			// If the name is something like "Aa.Bee.Cee",
			// Cee may actually be an inner class of Bee,
			// so it can only be loaded as "Aa.Bee$Cee".
			// Trying that next...
			String dollarName = replaceLastDotWithDollar(name);
			if (dollarName == null) {
				throw e;
			}
			return loadClassPrivate(dollarName);
		}
	}
	
	synchronized public Collection<String> getPotentialPackagesForClass(String cls) {
		return classInfo.getPotentialPackagesForClass(cls);
	}
	
	private static String replaceLastDotWithDollar(String s) {
		int lastDot = s.lastIndexOf('.');
		if (lastDot == -1) {
			return null;
		}
		StringBuffer buf = new StringBuffer(s);
		buf.setCharAt(lastDot, '$');
		return buf.toString();
	}
	
	synchronized public Collection<String> getClassNameStartingWith(String s) {
		return classInfo.getClassNameStartingWith(s);
	}

	synchronized public Collection<String> getFullClassNamesStartingWith(String part) {
		return classInfo.getFullClassNameStartingWith(part);
	}
	
	public class CompilationUnit {
		public final String className;
		public final String source;
		public CompilationUnit(String className, String source) {
			this.className = className;
			this.source = source;
		}
	}

	public Map<String, List<Diagnostic<? extends JavaFileObject>>> compileAll(Map<String, String> classSources) {
		
		LinkedList<JavaFileObject> compilationUnits = new LinkedList<JavaFileObject>();
		for (Entry<String, String> e : classSources.entrySet()) {
			compilationUnits.add(new MemorySource(e.getKey(), e.getValue()));
		}
		
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
		
		List<String> options = new ArrayList<String>();
		
		if (classPath != null) {
			List<String> classPathOpt = Arrays.asList("-classpath", classPath);
			options.addAll(classPathOpt);
		}
		
		// -proc:none means that compilation takes place without annotation processing
		options.add("-proc:none");

		// compiles the thing
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		// Must create a new classloader every time because its impossible
		// to update a class in the same classloader.
		xcl = new SpecialClassLoader(ucl, xcl);
		
		SpecialJavaFileManager specu = new SpecialJavaFileManager(compiler.getStandardFileManager(collector, null, null), xcl);
		
		CompilationTask task = compiler.getTask(null, specu, collector,
				options, null, compilationUnits);
		
		if (task != null) {
			task.call();
		}
		else {
			System.err.println("WARNING: could not get compilation task");
			return Collections.emptyMap();
		}
		
		TreeMap<String, List<Diagnostic<? extends JavaFileObject>>> errors =
				new TreeMap<String, List<Diagnostic<? extends JavaFileObject>>>();
		for (Entry<String, String> e : classSources.entrySet()) {
			List<Diagnostic<? extends JavaFileObject>> ofThis = errorsInThisClass(e.getKey(), collector.getDiagnostics());
			if (ofThis.isEmpty()) {
				saveSuccesful(e.getKey(), e.getValue());
			}
			errors.put(e.getKey(), ofThis);
		}
		
		return errors;
	}
}
