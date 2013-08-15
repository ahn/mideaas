package org.vaadin.aceeditor.java.util;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.aceeditor.Suggestion;
import org.vaadin.aceeditor.java.util.CompilerSuggester.ParseResult;
import org.vaadin.aceeditor.java.util.CompilerSuggester.SuggestionGenerator;

public class StdJavaSuggestionGenerator implements SuggestionGenerator {

	public List<Suggestion> getSuggestions(ParseResult parseResult) {

		String identifierStartsWith = parseResult.wordUnderCursor;

		List<Suggestion> suggs = new LinkedList<Suggestion>();

		for (MyMethodInfo m : parseResult.methods) {
			String mn = m.getName();
			if (mn.startsWith(identifierStartsWith)) {
				suggs.add(createMethodSuggestion(m,
						identifierStartsWith.length()));
			}
		}

		for (MyVariableInfo s : parseResult.variables) {
			String vn = s.getName();
			if (vn.startsWith(identifierStartsWith)) {
				String descr = "Field <strong>" + vn + "</strong> ("
						+ s.getClassName() + ")";
				String su = vn.substring(identifierStartsWith.length());
				suggs.add(new Suggestion(vn, descr, su));
			}
		}

		return suggs;
	}
	
	private Suggestion createMethodSuggestion(MyMethodInfo method, int cutFromStart) {
		String name = method.getName();
		String descr = method.getReturnType() + " <strong>" + name + "</strong>";
		int selStart = name.indexOf('(') + 1;
		int selEnd = name.indexOf(')');
		if (selStart == selEnd) {
			// no params -> no selection between ()'s
			selStart = name.length();
			selEnd = name.length();
		}
		return new Suggestion(name, descr, name.substring(cutFromStart));
	}

}
