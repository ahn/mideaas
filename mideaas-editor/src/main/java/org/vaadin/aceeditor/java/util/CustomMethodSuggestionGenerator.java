package org.vaadin.aceeditor.java.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vaadin.aceeditor.Suggestion;
import org.vaadin.aceeditor.client.Util;
import org.vaadin.aceeditor.java.util.CompilerSuggester.ParseResult;
import org.vaadin.aceeditor.java.util.CompilerSuggester.SuggestionGenerator;

/**
 * Generates c.addListener(new Listener() { ... }) kind of suggestions.
 * 
 */
public class CustomMethodSuggestionGenerator implements SuggestionGenerator {

	private List<SuggMethod> suggMethods;

	private InMemoryCompiler compiler;

	// Eg: MyClass:myMethod(ParamType1, ParamType2) "Sugg 4 myMethod"
	// group 1: methodName OR ClassName:methodName
	// group 2: method parameters
	// group 3: description
	private static Pattern suggestionLine = Pattern
			.compile("(\\S+)\\(([^)]+)\\)\\s+\"([^\"]+)\"");

	public CustomMethodSuggestionGenerator(InMemoryCompiler compiler) {
		this.compiler = compiler;
	}

	public void setSuggestions(String s) {
		suggMethods = suggMethodsFromStr(s);
	}

	// http://stackoverflow.com/questions/180097/dynamically-find-the-class-that-represents-a-primitive-java-type
	private static Map<String, Class<?>> builtInMap = new HashMap<String, Class<?>>();
	static {
		builtInMap.put("int", Integer.TYPE);
		builtInMap.put("long", Long.TYPE);
		builtInMap.put("double", Double.TYPE);
		builtInMap.put("float", Float.TYPE);
		builtInMap.put("bool", Boolean.TYPE);
		builtInMap.put("char", Character.TYPE);
		builtInMap.put("byte", Byte.TYPE);
		builtInMap.put("void", Void.TYPE);
		builtInMap.put("short", Short.TYPE);
	}

	private Class<?> typeOf(String str) throws ClassNotFoundException {
		if (builtInMap.containsKey(str)) {
			return builtInMap.get(str);
		} else {
//			throw new RuntimeException("Not implemented"); // TODO
			Class<?> xd = compiler.loadClassTryDollar(str);
			return xd;
		}
	}

	enum SuggestionType {
		ANONYMOUS, NEW, TYPE, LITERAL;
		static SuggestionType fromString(String str) {
			final String upper = str.toUpperCase();
			if (upper.equals("ANONYMOUS")) {
				return ANONYMOUS;
			} else if (upper.equals("NEW")) {
				return NEW;
			} else if (upper.equals("TYPE")) {
				return TYPE;
			} else {
				return LITERAL;
			}
		}
	};

	private static class Param {
		SuggestionType suggType;
		String typeName;
		String str;

		public Param(SuggestionType type, String typeName, String str) {
			super();
			this.suggType = type;
			this.typeName = typeName;
			this.str = str;
		}

		@Override
		public String toString() {
			return typeName + ":" + suggType.toString();
		}

		public boolean isOfType(String t) {
			return typeName.equals(t) || typeName.endsWith("." + t);
		}

		public String htmlDescr() {
			if (suggType == SuggestionType.ANONYMOUS) {
				return "&lt;Skeleton for anonymous " + simpleNameOf(typeName)
						+ "&gt;";
			} else if (suggType == SuggestionType.NEW) {
				return "new " + simpleNameOf(typeName);
			} else if (suggType == SuggestionType.LITERAL) {
				return str;
			} else if (suggType == SuggestionType.TYPE) {
				return typeName;
			}
			return "hmm...";
		}

	}

	private static class SuggMethod {
		String className;
		String name;
		LinkedList<Param> params;
		String descr;

		public SuggMethod(String className, String name,
				LinkedList<Param> params, String descr) {
			super();
			this.className = className;
			this.name = name;
			this.params = params;
			this.descr = descr;
		}

		@Override
		public String toString() {
			String s = name + " ";
			for (Param p : params) {
				s += p.toString() + " ";
			}
			return s;
		}

		boolean belongsToClass(String cls) {
			return this.className == null || className.equals(cls)
					|| className.endsWith("." + cls);
		}

		String htmlDescr() {
			StringBuilder descr = new StringBuilder();
			descr.append(this.descr).append(" - ");
			descr.append(name).append('(');
			boolean first = true;
			for (Param p : params) {
				if (!first) {
					descr.append(", ");
				}
				descr.append(p.htmlDescr());
				first = false;
			}
			descr.append(')');
			return descr.toString();
		}
	}

	private static String simpleNameOf(String name) {
		int lastDot = name.lastIndexOf('.');
		if (lastDot == -1) {
			return name;
		} else {
			return name.substring(lastDot + 1);
		}
	}

	private static LinkedList<SuggMethod> suggMethodsFromStr(String str) {
		String lines[] = str.split("\\r?\\n");
		LinkedList<SuggMethod> methods = new LinkedList<SuggMethod>();
		for (String line : lines) {
			SuggMethod sm = suggMethodFromStr(line);
			if (sm != null) {
				methods.add(sm);
			} else {
				System.err
						.println("WARNING: ignoring invalid suggestion line: >>>"
								+ line + "<<<");
			}

		}
		return methods;
	}

	private static SuggMethod suggMethodFromStr(String str) {
		Matcher m = suggestionLine.matcher(str);
		if (!m.matches()) {
			return null;
		}
		String methodStr = m.group(1);
		String paramsStr = m.group(2);
		String descr = m.group(3);

		String[] classMethod = methodStr.split(":", 2);
		String cls;
		String method;
		if (classMethod.length == 1) {
			cls = null;
			method = classMethod[0];
		} else {
			cls = classMethod[0];
			method = classMethod[1];
		}
		LinkedList<Param> params = paramsFromStr(paramsStr);
		return new SuggMethod(cls, method, params, descr);
	}

	private static LinkedList<Param> paramsFromStr(String str) {
		String[] paramStrs = str.split(",");

		LinkedList<Param> params = new LinkedList<Param>();
		for (int i = 0; i < paramStrs.length; ++i) {
			Param p = paramFromStr(paramStrs[i].trim());
			params.add(p);
		}
		return params;
	}

	private static Param paramFromStr(String str) {
		String[] pars = str.split("=", 2);
		SuggestionType type;
		if (pars.length == 1) {
			type = SuggestionType.TYPE;
		} else {
			type = SuggestionType.fromString(pars[1]);
		}

		if (type == SuggestionType.LITERAL) {
			return new Param(type, pars[0], pars[1]);
		} else {
			return new Param(type, pars[0], null);
		}
	}

	CustomMethodSuggestionGenerator(String methodName,
			List<List<String>> paramClassNames) throws ClassNotFoundException {

		for (List<String> params : paramClassNames) {
			LinkedList<Class<?>> clss = new LinkedList<Class<?>>();
			for (String p : params) {
				Class<?> cls = Class.forName(p);
				clss.add(cls);
			}
		}

	}

	/* @Override */
	public List<Suggestion> getSuggestions(ParseResult parseResult) {
		if (suggMethods == null) {
			return Collections.emptyList();
		}

		String identifierStartsWith = parseResult.wordUnderCursor;

		List<Suggestion> suggs = new LinkedList<Suggestion>();

		final String indent = Util.indentationStringOfCursorLine(
				parseResult.source, parseResult.cursor);

		// TODO: could optimize.
		// Now we're comparing each item in parseResult.methods to each item in
		// suggMethods
		// Could group the suggMethods by class, for example.

		for (MyMethodInfo m : parseResult.methods) {
			String mn = m.getName();
			if (mn.startsWith(identifierStartsWith)) {
				List<Suggestion> ss = createSuggestionsForMethod(m,
						identifierStartsWith, parseResult, indent);
				suggs.addAll(ss);
			}
		}

		return suggs;
	}

	private List<Suggestion> createSuggestionsForMethod(MyMethodInfo method,
			String startsWith, ParseResult parseResult, String indent) {

		String name = method.getName();
		int numParams = method.nmbrOfParameters();

		LinkedList<Suggestion> suggs = new LinkedList<Suggestion>();

		// TODO: if classInfo is eg. an anonymous class inside a class A,
		// the classInfo.getName() is empty.
		// Thus, if suggesting inside the anonymous class,
		// suggestions A:metodi() won't be found.

		for (SuggMethod sm : suggMethods) {
			if (sm.belongsToClass(parseResult.classInfo.getName())
					&& name.startsWith(sm.name)
					&& numParams == sm.params.size()) {
				boolean paramsOk = true;
				int i = 0;
				for (Param p : sm.params) {
					String param = method.getParameterTypes().get(i);

					if (!p.isOfType(param.split("\\s", 2)[0])) {
						paramsOk = false;
						break;
					}
					++i;
				}
				if (paramsOk) {
					int cur = parseResult.cursor;
					suggs.add(createSuggestion(sm, cur - startsWith.length(),
							cur, indent));
				}
			}

		}

		return suggs;
	}

	private Suggestion createSuggestion(SuggMethod sm, int start, int end,
			String indent) {

		// TODO: could optimize.
		// We could reuse the already generated suggestions,
		// no need to generate them on each call to getSuggestions...

		final StringBuilder su = new StringBuilder();
		su.append(sm.name).append("(");

		boolean first = true;
		for (Param p : sm.params) {
			if (!first) {
				su.append(", ");
			}
			if (p.suggType == SuggestionType.ANONYMOUS) {
				Class<?> cls;
				try {
					cls = typeOf(p.typeName);
					String skel = CodeGenerator.getAnynomousSkeletonOf(cls,
							indent);
					su.append(skel);
				} catch (ClassNotFoundException e) {
					System.err.println("WARNING: class " + p.typeName + " not found, can't create anynomous skeleton");
					su.append(p.typeName);
				}

			} else if (p.suggType == SuggestionType.NEW) {
				Class<?> cls;
				try {
					cls = typeOf(p.typeName);
					String skel = CodeGenerator.getNewOf(cls, indent);
					su.append(skel);
				} catch (ClassNotFoundException e) {
					System.err.println("WARNING: class " + p.typeName + " not found, can't create new skeleton");
					su.append(p.typeName);
				}
			} else if (p.suggType == SuggestionType.TYPE) {
				su.append(p.typeName);
			} else if (p.suggType == SuggestionType.LITERAL) {
				su.append(p.str);
			}
			first = false;
		}
		su.append(");");

		String suStr = su.toString();
//		int selStart = su.indexOf("// TODO");
//		int selEnd = su.indexOf("\n", selStart);
		
		// TODO: better description
		Suggestion s = new Suggestion(sm.descr, sm.htmlDescr(), suStr);
		return s;
	}

}
