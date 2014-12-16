package org.vaadin.mideaas.ide;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class IdeUtil {
	
	private static final Logger log = Logger.getLogger(IdeUtil.class.getName());

	public static IdeProject createProject(String name, Map<String, String> contents, IdeConfiguration config, File workDir) {
		IdeProject project = config.createProject(IdeProject.randomProjectId(), name, contents, workDir);
		for (Entry<String, String> e : contents.entrySet()) {
			project.putDoc(e.getKey(), e.getValue());
		}
		return project;
	}

	public static Map<String, String> readContentsFromDir(File dir, IdeConfiguration config) {
		int le = dir.getPath().length() + 1; // + 1 for the slash after dir
		Map<String, String> contents = new TreeMap<String, String>();
		for (File f : readFilesFrom(dir, config)) {
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

	private static Collection<File> readFilesFrom(File dir, final IdeConfiguration config) {
		IOFileFilter filter = new IOFileFilter() {
			@Override
			public boolean accept(File f, String arg1) {
				return config.isProjectFile(f.getName());
			}
			
			@Override
			public boolean accept(File f) {
				return config.isProjectFile(f.getName());
			}
		};
		return FileUtils.listFiles(dir, filter, filter);
	}


	
	public static void saveFilesToPath(Map<String, String> files, Path path) {
		Map<String,String> empty = Collections.emptyMap();
		saveChangedFilesToPath(files, path, empty); 
	}

	public static void saveChangedFilesToPath(Map<String, String> files,
			Path path, Map<String, String> written) {
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
			try {
				Files.write(path.resolve(f), e.getValue().getBytes());
			} catch (IOException e1) {
				log.log(Level.WARNING, "Could not write " + path.resolve(f));
			}
		}
	}



}
