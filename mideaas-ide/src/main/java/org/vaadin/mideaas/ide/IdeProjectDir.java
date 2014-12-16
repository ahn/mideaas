package org.vaadin.mideaas.ide;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Project on a disk.
 * 
 */
public class IdeProjectDir {
	
	private static final Logger log = Logger.getLogger(IdeProjectDir.class.getName());

	private final IdeProject project;
	private final File dir;
	
	/**
	 * Storing written files so we can avoid writing files that have not been changed.
	 */
	private IdeProjectSnapshot writtenSnapshot;
	
	public IdeProjectDir(IdeProject project, File dir) {
		this.project = project;
		this.dir = dir;
	}
	
	public synchronized void writeToDisk() throws IOException {
		log.log(Level.INFO, "Writing project " + project.getId() + " to " + dir);
		IdeProjectSnapshot written = getWrittenSnapshot();
		IdeProjectSnapshot snapshot = project.getSnapshot();
		if (written==null) {
			snapshot.writeToDisk(dir.toPath());
		}
		else {
			snapshot.changedOrNewFiles(written).writeToDisk(dir.toPath());
			written.removeFilesNotInOther(dir.toPath(), snapshot);
		}
		setWrittenSnapshot(snapshot);
	}
	
	private IdeProjectSnapshot getWrittenSnapshot() {
		return writtenSnapshot;
	}
	
	private void setWrittenSnapshot(IdeProjectSnapshot snapshot) {
		writtenSnapshot = snapshot;
	}

	public File getDir() {
		return dir;
	}
}
