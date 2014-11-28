package org.vaadin.mideaas.editor;

import java.util.Map.Entry;
import java.util.UUID;

import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;

import com.steadystate.css.parser.CSSOMParser;

import elemental.css.CSSCharsetRule;

public class Util {

	
	public static MultiUserProject projectFromGist(GHGist gist) {
		
		String name = UUID.randomUUID().toString();
		
		MultiUserProject project = new MultiUserProject(name);
		
		System.out.println(gist.getUrl());
		for (Entry<String, GHGistFile> e : gist.getFiles().entrySet()) {
			IdeDoc ideDoc = docFromGistFile(e.getValue());
			project.putDoc(e.getKey(), ideDoc);
		}
		
		return project;
	}

	private static IdeDoc docFromGistFile(GHGistFile file) {
		AceMode mode = aceModeForGistFile(file);
		MultiUserDoc mud = new MultiUserDoc(
				new AceDoc(file.getContent()),
				filterForGistFile(file),
				upwardsGuardForMode(mode),
				downwardsGuardForGistFile(file),
				checkerForGistFile(file));
		System.out.println(mode);
		return new IdeDoc(mud, mode);
	}

	private static AceMode aceModeForGistFile(GHGistFile file) {
		String lang = file.getLanguage();
		if (lang==null) {
			return AceMode.text;
		}
		try {
			return AceMode.valueOf(aceLangFromGithubLang(lang));
		}
		catch (IllegalArgumentException e) {
			return AceMode.text;
		}
	}

	private static String aceLangFromGithubLang(String lang) {
		System.out.println("lang " + lang);
		return lang.toLowerCase();
	}

	private static Filter filterForGistFile(GHGistFile file) {
		// TODO Auto-generated method stub
		
		return null;
	}

	private static Guard upwardsGuardForMode(AceMode mode) {
		
		switch (mode) {
		case css: return new CSSGuard();
		default: return null;
		}
		
	}

	private static Guard downwardsGuardForGistFile(GHGistFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	private static AsyncErrorChecker checkerForGistFile(GHGistFile file) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static String INDEX_HTML = "<!DOCTYPE html>\n<html>\n    <head>\n        <title>Hello</title>\n"
			+ "        <link href=\"./style.css\" rel=\"stylesheet\" />\n    <head>\n"
			+ "    <body>\n        <p>Hello!</p>\n    </body>\n</html>\n";
	
	private static String STYLE_CSS = "body {\n    color: red;\n}\n";

	public static MultiUserProject createDemoProject() {
		String name = UUID.randomUUID().toString();
		
		MultiUserProject project = new MultiUserProject(name);
		
		MultiUserDoc doc1 = new MultiUserDoc(new AceDoc(INDEX_HTML), null, null, null, null);
		project.putDoc("index.html", new IdeDoc(doc1, AceMode.html));

		MultiUserDoc doc2 = new MultiUserDoc(new AceDoc(STYLE_CSS), null, new CSSGuard(), null, null);
		project.putDoc("style.css", new IdeDoc(doc2, AceMode.css));
		
		return project;
	}
	

}
