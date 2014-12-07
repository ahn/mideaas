package org.vaadin.mideaas.ide;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.AsyncErrorChecker;
import org.vaadin.mideaas.editor.DocDiffMediator.Filter;
import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
import org.vaadin.mideaas.editor.MultiUserDoc;

public class IdeUtil {

	
//	public static IdeProject projectFromGist(GHGist gist, ProjectCustomizer cust) {
//		
//		String id = UUID.randomUUID().toString();
//		
//		Map<String, String> files = new TreeMap<String, String>();
//		for (Entry<String, GHGistFile> e : gist.getFiles().entrySet()) {
//			files.put(e.getKey(), e.getValue().getContent());
//		}
//		
//
//		
//		IdeProject project = cust.createProject(id, gist.getId(), files);
//		
//		for (Entry<String, GHGistFile> e : gist.getFiles().entrySet()) {
//			IdeDoc ideDoc = docFromGistFile(e.getValue(), cust);
//			project.putDoc(e.getKey(), ideDoc);
//		}
//		
//		return project;
//	}

//	private static IdeDoc docFromGistFile(GHGistFile file, ProjectCustomizer cust) {
//		MultiUserDoc mud = customizedDoc(file.getFileName(), file.getContent(), cust);
//		return new IdeDoc(mud, aceModeForGistFile(file));
//	}

//	private static AceMode aceModeForGistFile(GHGistFile file) {
//		String lang = file.getLanguage();
//		if (lang==null) {
//			return AceMode.text;
//		}
//		try {
//			return AceMode.valueOf(aceLangFromGithubLang(lang));
//		}
//		catch (IllegalArgumentException e) {
//			return AceMode.text;
//		}
//	}

	private static String aceLangFromGithubLang(String lang) {
		System.out.println("lang " + lang);
		return lang.toLowerCase();
	}
	


	private static MultiUserDoc customizedDoc(String filename, String content, ProjectCustomizer cust, IdeProject project) {
		Guard upGuard = cust.getUpwardsGuardFor(filename);
		Guard downGuard = cust.getDownwardsGuardFor(filename);
		Filter filter = cust.getFilterFor(filename);
		AsyncErrorChecker checker = cust.getErrorCheckerFor(filename, project);
		return new MultiUserDoc(new AceDoc(content), filter, upGuard, downGuard, checker);
	}
	


	public static IdeDoc createDoc(String filename, String content, IdeConfiguration config, IdeProject project) {
		MultiUserDoc doc = customizedDoc(filename, content, config.getProjectCustomizer(project), project);
		return new IdeDoc(doc, aceModeForFilename(filename));
	}
	
//	public static IdeProject createDemoProject(ProjectCustomizer cust) {
//		String id = UUID.randomUUID().toString();
//		
//		Map<String,String> noFiles = Collections.emptyMap();
//		IdeProject project = cust.createProject(id, "Example project", noFiles);
//		
//		MultiUserDoc doc1 = customizedDoc("index.html", INDEX_HTML, cust);
//		project.putDoc("index.html", new IdeDoc(doc1, AceMode.html));
//
//		MultiUserDoc doc2 = customizedDoc("style.css", STYLE_CSS, cust);
//		project.putDoc("style.css", new IdeDoc(doc2, AceMode.css));
//		
//		return project;
//	}

	public static IdeProject createProject(String name, Map<String, String> contents, IdeConfiguration config) {
		IdeProject project = config.createProject(UUID.randomUUID().toString(), name, contents);
		for (Entry<String, String> e : contents.entrySet()) {
			MultiUserDoc doc = customizedDoc(e.getKey(), e.getValue(), config.getProjectCustomizer(project), project);
			project.putDoc(e.getKey(), new IdeDoc(doc, aceModeForFilename(e.getKey())));
		}
		return project;
	}

	private static AceMode aceModeForFilename(String filename) {
		return AceMode.forFile(filename);
	}

	public static Map<String, String> readContentsFromDir(File dir) {
		int le = dir.getPath().length() + 1; // + 1 for the slash after dir
		Map<String, String> contents = new TreeMap<String, String>();
		for (File f : readFilesFrom(dir)) {
			try {
				String content = readFileContent(f);
				String path = f.getPath().substring(le).replace("\\", "/");
				contents.put(path, content);
			} catch (IOException e) {
				e.printStackTrace();
				// XXX ignoring...
			}
		}
		return contents;
	}
	
	private static String readFileContent(File f) throws IOException {
		return new String(Files.readAllBytes(f.toPath()));
	}

	private static Collection<File> readFilesFrom(File dir) {
		IOFileFilter filter = new IOFileFilter() {
			@Override
			public boolean accept(File f, String arg1) {
				return !f.getName().startsWith(".");
			}
			
			@Override
			public boolean accept(File f) {
				return !f.getName().startsWith(".");
			}
		};
		return FileUtils.listFiles(dir, filter, filter);
	}


	
	public static void saveFilesToPath(Map<String, String> files, Path path) throws IOException {
		for (Entry<String, String> e : files.entrySet()) {
			String f = e.getKey();
			String filename = f.substring(f.lastIndexOf("/")+1);
			if (f.length() > filename.length()) {
				Path dir = path.resolve(f.substring(0, f.length() - filename.length() - 1));
				dir.toFile().mkdirs();
			}
			Files.write(path.resolve(f), e.getValue().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
	}



}
