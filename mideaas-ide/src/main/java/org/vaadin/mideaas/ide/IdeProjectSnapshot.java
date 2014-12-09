package org.vaadin.mideaas.ide;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

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
	
	public void writeToDisk(Path path) throws IOException {
		IdeUtil.saveFilesToPath(files, path);
	}
	
	public void writeChangedToDisk(Path path, IdeProjectSnapshot previous) throws IOException {
		IdeUtil.saveChangedFilesToPath(files, path, previous.files);
	}

}
