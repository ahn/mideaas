package org.vaadin.mideaas.ide;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class IdeUtil {

//	private static MultiUserDoc customizedDoc(String filename, String content, ProjectCustomizer cust, IdeProject project) {
//		Guard upGuard = cust.getUpwardsGuardFor(filename);
//		Guard downGuard = cust.getDownwardsGuardFor(filename);
//		Filter filter = cust.getFilterFor(filename);
//		AsyncErrorChecker checker = cust.getErrorCheckerFor(filename, project);
//		return new MultiUserDoc(new AceDoc(content), filter, upGuard, downGuard, checker);
//	}
//
//	public static IdeDoc createDoc(String filename, String content, ProjectCustomizer customizer, IdeProject project) {
//		MultiUserDoc doc = customizedDoc(filename, content, customizer, project);
//		return new IdeDoc(doc, aceModeForFilename(filename));
//	}

	public static IdeProject createProject(String name, Map<String, String> contents, IdeConfiguration config) {
		IdeProject project = config.createProject(UUID.randomUUID().toString(), name, contents);
		for (Entry<String, String> e : contents.entrySet()) {
			project.putDoc(e.getKey(), e.getValue());
		}
		return project;
	}
//
//	private static AceMode aceModeForFilename(String filename) {
//		return AceMode.forFile(filename);
//	}

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
		Map<String,String> empty = Collections.emptyMap();
		saveChangedFilesToPath(files, path, empty); 
	}

	public static void saveChangedFilesToPath(Map<String, String> files,
			Path path, Map<String, String> written) throws IOException {
		for (Entry<String, String> e : files.entrySet()) {
			if (e.getValue().equals(written.get(e.getKey()))) {
				continue;
			}
			String f = e.getKey();
			String filename = f.substring(f.lastIndexOf("/")+1);
			if (f.length() > filename.length()) {
				Path dir = path.resolve(f.substring(0, f.length() - filename.length() - 1));
				dir.toFile().mkdirs();
			}
			Files.write(path.resolve(f), e.getValue().getBytes());
		}
	}



}
