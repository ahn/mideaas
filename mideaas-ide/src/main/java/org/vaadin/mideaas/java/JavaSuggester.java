package org.vaadin.mideaas.java;

import java.util.List;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.aceeditor.Suggestion;
import org.vaadin.mideaas.java.util.CompilerSuggester;
import org.vaadin.mideaas.java.util.CustomMethodSuggestionGenerator;
import org.vaadin.mideaas.java.util.InMemoryCompiler;
import org.vaadin.mideaas.java.util.StdJavaSuggestionGenerator;

public class JavaSuggester implements Suggester {
	
	

	private static final String vaadinSuggestions = "addListener(com.vaadin.event.FieldEvents.FocusListener=ANONYMOUS) \"Add anon. FocusListener\"\n"
//			+ "addListener(com.vaadin.event.FieldEvents.BlurListener=ANONYMOUS) \"Add anon. BlurListener\"\n"
//			+ "addListener(com.vaadin.data.Property.ValueChangeListener=ANONYMOUS) \"Add anon. ValueChangeListener\"\n"
			+ "addClickListener(com.vaadin.ui.Button.ClickListener=ANONYMOUS) \"Add anon. ClickListener\"\n";
			// TODO: more?
	
	private final CompilerSuggester compSugger;

	public JavaSuggester(InMemoryCompiler compiler, String className) {
		compSugger = new CompilerSuggester(compiler, className);
		CustomMethodSuggestionGenerator vaadinSG = new CustomMethodSuggestionGenerator(compiler);
		vaadinSG.setSuggestions(vaadinSuggestions);
		compSugger.addSuggestionGenerator(vaadinSG);
		compSugger.addSuggestionGenerator(new StdJavaSuggestionGenerator());
	}
	
	@Override
	public List<Suggestion> getSuggestions(String text, int cursor) {
		return compSugger.getSuggestions(text, cursor);
	}

	@Override
	public String applySuggestion(Suggestion sugg, String text, int cursor) {
		if (sugg instanceof ImportingSuggestion) {
			ensureImport(((ImportingSuggestion)sugg).getImportClass());
		}
		String ins = sugg.getSuggestionText();
		String s1 = text.substring(0,cursor) + ins + text.substring(cursor);
		return s1;
	}

	// TODO
	
	private void ensureImport(String cls) {
//		try {
//			ControllerCode c = new ControllerCode(codeMud.getBase().getText());
//			String code1 = c.getCode();
//			c.ensureImport(cls);
//			ServerSideDocDiff d = ServerSideDocDiff.diff(new AceDoc(code1), new AceDoc(c.getCode()));
//			codeMud.tryToApply(d, null);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
