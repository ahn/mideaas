package org.vaadin.mideaas.ide;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dev.util.collect.HashSet;

/**
 * An immutable class containing the project files at some moment.
 * 
 *
 */
public class IdeProjectSnapshot {
	public static final IdeProjectSnapshot EMPTY = new IdeProjectSnapshot();
	private final Map<String, String> files;

	public IdeProjectSnapshot(Map<String, String> files) {
		this.files = files;
	}
	
	public IdeProjectSnapshot() {
		files = Collections.emptyMap();
	}

	public Map<String, String> getFiles() {
		return Collections.unmodifiableMap(files);
	}
	
	public void writeToDisk(Path path) {
		IdeUtil.saveFilesToPath(files, path);
	}
	
	public IdeProjectSnapshot changedOrNewFiles(IdeProjectSnapshot previous) {
		HashMap<String, String> ch = new HashMap<String, String>();
		for (Entry<String, String> e : files.entrySet()) {
			String prev = previous.files.get(e.getKey());
			if (!e.getValue().equals(prev)) {
				ch.put(e.getKey(), e.getValue());
			}
		}
		return new IdeProjectSnapshot(ch);
	}

	private Collection<String> minus(IdeProjectSnapshot shot) {
		HashSet<String> missing = new HashSet<String>(files.keySet());
		missing.removeAll(shot.files.keySet());
		return missing;
	}

	public void removeFilesNotInOther(Path p, IdeProjectSnapshot other) {
		for (String name : this.minus(other)) {
			try {
				Files.deleteIfExists(p.resolve(name));
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
		
	}

}
