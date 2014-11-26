package org.vaadin.mideaas.ide.java.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;

import org.vaadin.aceeditor.Suggestion;
import org.vaadin.mideaas.ide.java.ImportingSuggestion;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;


public class CompilerSuggester {
	
	private final InMemoryCompiler compiler;
	private final String className;
	
	// TODO: some refactoring still to be done...
		
	private static Pattern newClass = Pattern.compile(".*[^\\w.]new\\s+(\\w*)$", Pattern.DOTALL);
	private static Pattern dotWord = Pattern.compile(".*\\W(\\w+)\\s*\\.\\s*(\\w*)$", Pattern.DOTALL);
	private static Pattern word = Pattern.compile(".*\\W(\\w*)$", Pattern.DOTALL);
	private static final Pattern importPackage =
			Pattern.compile(".*\\Wimport\\s+([\\w.]+)$", Pattern.DOTALL);

	interface SuggestionGenerator {
		List<Suggestion> getSuggestions(ParseResult parseResult);
	}

	private LinkedList<SuggestionGenerator> generators = new LinkedList<SuggestionGenerator>();

	class ParseResult {
		final String source;
		final int cursor;
		final MyClassInfo classInfo;
		final TreeSet<MyMethodInfo> methods;
		final TreeSet<MyVariableInfo> variables;
		final String varNameOfClass;
		final String wordUnderCursor;

		ParseResult(String source, int cursor, MyClassInfo classInfo,
				TreeSet<MyMethodInfo> methods,
				TreeSet<MyVariableInfo> variables, String varNameOfClass,
				String wordUnderCursor) {
			super();
			this.source = source;
			this.cursor = cursor;
			this.classInfo = classInfo;
			this.methods = methods;
			this.variables = variables;
			this.varNameOfClass = varNameOfClass;
			this.wordUnderCursor = wordUnderCursor;
		}
		
		
	}

	public CompilerSuggester(InMemoryCompiler compiler, String fullJavaClassName) {
		this.compiler = compiler;
		this.className = fullJavaClassName;
	}

	public void addSuggestionGenerator(SuggestionGenerator sg) {
		generators.add(sg);
	}

	public List<Suggestion> getSuggestions(String source, int cursor) {

		if (source == null || source.length() == 0) {
			return Collections.emptyList();
		}
		
		int start = Math.max(0, cursor-30);
		String beforeCursor = source.substring(start, cursor);
		
		Matcher m = importPackage.matcher(beforeCursor);
		if (m.matches()) {
			return getImportSuggestions(m.group(1));
		}
		
		m = newClass.matcher(beforeCursor);
		if (m.matches()) {
			return getClassSuggestions(m.group(1), 0);
		}
		
		SourceScanner scanner = scannerFor(source);

		if (scanner==null) {
			return Collections.emptyList();
		}
		
		m = dotWord.matcher(beforeCursor);
		if (m.matches()) {
			if ("this".equals(m.group(1))) {
				return getThisDotWordSuggestions(scanner, source, m.group(1), m.group(2), cursor);
			}
			else {
				return getWordDotWordSuggestions(scanner, source, m.group(1), m.group(2), cursor);
			}
		}
		
		m = word.matcher(beforeCursor);
		if (m.matches()) {
			List<Suggestion> suggs = new LinkedList<Suggestion>();
			suggs.addAll( getWordSuggestions(scanner, source, m.group(1), cursor) );
			if (suggs.size()<60) {
				suggs.addAll( getClassSuggestions(m.group(1), suggs.size()));
			}
			return suggs;
		}
		
		return Collections.emptyList();
	}

	private List<Suggestion> getImportSuggestions(String part) {
		
		Collection<String> classes = compiler.getFullClassNamesStartingWith(part);
		
		LinkedList<Suggestion> suggs = new LinkedList<Suggestion>();
		int i = 0;
		for (String s : classes) {
			if (i++ == 100) {
				suggs.add(new Suggestion("<not all listed>", "Listing only 100 of the total of "+classes.size()+" suggestions"));
				break;
			}
			String val = s.substring(part.length());
			suggs.add(new Suggestion(s, s, val));
		}
		return suggs;
	}

	private List<Suggestion> getClassSuggestions(String word, int listedSoFar) {
		LinkedList<Suggestion> suggs = new LinkedList<Suggestion>();
		int i = listedSoFar;
		Collection<String> classes = compiler.getClassNameStartingWith(word);
		for (String s : classes) {
			if (i++ == 100) {
				suggs.add(new Suggestion("<not all listed>", "Listing only 100 of the total of "+classes.size()+" suggestions"));
				break;
			}
			String val = s.substring(word.length());
			Collection<String> packs = compiler.getPotentialPackagesForClass(s);
			for (String p : packs) {
				String fullName = p + "." + s;
				suggs.add(new ImportingSuggestion(s, fullName, val, fullName));
			}
		}
		return suggs;
	}

	private List<Suggestion> getWordSuggestions(SourceScanner scanner,
			String source, String word, int cursor) {
		
		MyClassInfo myClassInfo = scanner.getClassUnderCursor(cursor);
		if (myClassInfo == null) {
			System.err.println("no class under cursor");
			return Collections.emptyList();
		}
		MyMethodInfo methodInfo = myClassInfo.getMethodUnderCursor(cursor);
		if (methodInfo==null) {
			System.err.println("no method under cursor");
			return Collections.emptyList();
		}
		
		TreeSet<MyMethodInfo> myMethods = new TreeSet<MyMethodInfo>();
		TreeSet<MyVariableInfo> myFields = new TreeSet<MyVariableInfo>();
		
		List<MyMethodInfo> staticMethods = myClassInfo.getStaticMethods();
		myMethods.addAll(staticMethods);

		List<MyMethodInfo> methods = myClassInfo.getMethods();
		myMethods.addAll(methods);

		myMethods.addAll(myClassInfo.getParentClassMethods());

		// add all the variables
		List<MyVariableInfo> staticVariables = myClassInfo
				.getStaticVariables();
		myFields.addAll(staticVariables);

		List<MyVariableInfo> classVariables = myClassInfo.getVariables();
		myFields.addAll(classVariables);

		List<MyVariableInfo> localVariables = methodInfo.getVariables();
		myFields.addAll(localVariables);

		myFields.addAll(myClassInfo.getParentClassFields());
		
		ParseResult pr = new ParseResult(source, cursor, myClassInfo, myMethods, myFields, word, word);
		return getSuggestionsForParseResult(pr);
	}

	private List<Suggestion> getThisDotWordSuggestions(SourceScanner scanner,
			String source, String beforeDot, String afterDot, int cursor) {
		MyClassInfo myClassInfo = scanner.getClassUnderCursor(cursor);
		if (myClassInfo == null) {
			System.err.println("no class under cursor");
			return Collections.emptyList();
		}
		TreeSet<MyMethodInfo> myMethods = new TreeSet<MyMethodInfo>();
		TreeSet<MyVariableInfo> myFields = new TreeSet<MyVariableInfo>();
		myMethods.addAll(myClassInfo.getMethods());
		myFields.addAll(myClassInfo.getVariables());

		// most likely the current method
		MyMethodInfo method = MyClassInfo.findMethod(myClassInfo, cursor);
		if (method != null) {
			List<MyVariableInfo> localVariables = method.getVariables();
			myFields.addAll(localVariables);
		}
		
		ParseResult pr = new ParseResult(source, cursor, myClassInfo, myMethods, myFields, beforeDot, afterDot);
		return getSuggestionsForParseResult(pr);
		
	}

	private List<Suggestion> getWordDotWordSuggestions(SourceScanner scanner, String source, String beforeDot, String afterDot, int cursor) {
		
		String className = getClassName(scanner, beforeDot, cursor);
		TreeSet<MyMethodInfo> myMethods = new TreeSet<MyMethodInfo>();
		TreeSet<MyVariableInfo> myFields = new TreeSet<MyVariableInfo>();
		MyClassInfo myClassInfo;
		if (className == null) {// collect static methods
			myClassInfo = scanner.getMyClassInfo(beforeDot);
			if (myClassInfo != null) {
				myMethods.addAll(myClassInfo.getStaticMethods());
				myFields.addAll(myClassInfo.getStaticVariables());
			}
		} else { // collect non-static methods
			myClassInfo = scanner.getMyClassInfo(className);
			if (myClassInfo != null) {
				myMethods.addAll(myClassInfo.getMethods());
				myFields.addAll(myClassInfo.getVariables());
			}
		}
		
		ParseResult pr = new ParseResult(source, cursor, myClassInfo, myMethods, myFields, beforeDot, afterDot);
		return getSuggestionsForParseResult(pr);
	}
	
	private List<Suggestion> getSuggestionsForParseResult(ParseResult pr) {
		List<Suggestion> suggs = new LinkedList<Suggestion>();
		for (SuggestionGenerator sg : generators) {
			suggs.addAll(sg.getSuggestions(pr));
		}
		return suggs;
	}

	private String getClassName(SourceScanner scanner, String identifierName, int cursorLocation) {
		List<MyVariableInfo> nodes = scanner.collectVariableNodesInScope(
				cursorLocation, identifierName);
		for (MyVariableInfo node : nodes) {
			if (node.getName().equals(identifierName)) {
				return node.getClassName();
			}
		}
		return null;
	}

	// processes source code given
	private SourceScanner scannerFor(String source) {
		
		CompilationTask comtask = compiler.getCompilationTask(
				className, source, new DiagnosticCollector<JavaFileObject>(), false);
		JavacTask task;
		if (comtask instanceof JavacTask) {
			task = (JavacTask) comtask;
		}
		else {
			System.err.println("ERROR: CompilationTask cannot be cast to JavacTask (some kind of ClassLoader issue I guess)");
			return null;
		}

		SourceScanner scanner = null;
		try {
			SourcePositions sourcePositions = Trees.instance(task)
					.getSourcePositions();

			Iterable<? extends CompilationUnitTree> trees = task.parse();
			
			for (CompilationUnitTree compilationUnitTree : trees) {
				scanner = new SourceScanner(compiler, compilationUnitTree,
						sourcePositions);
				scanner.scan(compilationUnitTree, null);
				//break; // XXX only the first tree, can there be more than 1?
				
			}
		}

		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		// TODO: Just silently catching all the exceptions may not be a good
		// idea...
		// There seem to be other exceptions thrown than IOExceptions by
		// task.parse()
		// Should catch them closer to where they happen / eliminate them.
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return scanner;
	}


}
