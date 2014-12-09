package org.vaadin.mideaas.app.java;

import japa.parser.ParseException;

import java.util.List;

import org.vaadin.aceeditor.ServerSideDocDiff;
import org.vaadin.aceeditor.Suggester;
import org.vaadin.aceeditor.Suggestion;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.app.VaadinProject;
import org.vaadin.mideaas.app.java.util.CompilerSuggester;
import org.vaadin.mideaas.app.java.util.CustomMethodSuggestionGenerator;
import org.vaadin.mideaas.app.java.util.InMemoryCompiler;
import org.vaadin.mideaas.app.java.util.StdJavaSuggestionGenerator;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.MultiUserDoc;
import org.vaadin.mideaas.editor.SharedDoc;
import org.vaadin.mideaas.ide.IdeDoc;
import org.vaadin.mideaas.ide.IdeUser;

public class JavaSuggester implements Suggester {
	
	

	private static final String vaadinSuggestions = "addListener(com.vaadin.event.FieldEvents.FocusListener=ANONYMOUS) \"Add anon. FocusListener\"\n"
//			+ "addListener(com.vaadin.event.FieldEvents.BlurListener=ANONYMOUS) \"Add anon. BlurListener\"\n"
//			+ "addListener(com.vaadin.data.Property.ValueChangeListener=ANONYMOUS) \"Add anon. ValueChangeListener\"\n"
			+ "addClickListener(com.vaadin.ui.Button.ClickListener=ANONYMOUS) \"Add anon. ClickListener\"\n";
			// TODO: more?
	
	private final CompilerSuggester compSugger;
	private final InMemoryCompiler compiler;
	private final IdeDoc doc;
	private final EditorUser user;

	public JavaSuggester(String className, InMemoryCompiler compiler, IdeDoc doc, EditorUser user) {
		this.compiler = compiler;
		this.doc = doc;
		this.user = user;

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

		SharedDoc childDoc = doc.getDoc().getChildDoc(user);
		if (childDoc == null) {
			System.err.println("WARNING: could not ensureImport");
		}
		try {
			ControllerCode c = new ControllerCode(doc.getDoc().getBaseText());
			String code1 = c.getCode();
			c.ensureImport(cls);
			ServerSideDocDiff d = ServerSideDocDiff.diff(new AceDoc(code1), new AceDoc(c.getCode()));
			childDoc.applyDiff(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
