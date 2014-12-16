package org.vaadin.mideaas.ide;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Project on a disk.
 * 
 */
public class IdeProjectDir {
	
	private static final Logger log = Logger.getLogger(IdeProjectDir.class.getName());

	private final IdeProjectWithWorkDir project;
	private final File dir;
	
	/**
	 * Storing written files so we can avoid writing files that have not been changed.
	 */
	private IdeProjectSnapshot writtenSnapshot;
	
	public IdeProjectDir(IdeProjectWithWorkDir project, File dir) {
		this.project = project;
		this.dir = dir;
	}
	
	public synchronized void writeToDisk() {
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

	public File toFile() {
		return dir;
	}
	
	public void destroy() {
		try {
			destroyOrThrow();
		} catch (IOException e) {
			log.log(Level.WARNING, "Could not destroy " + dir);
		}
	}

	private void destroyOrThrow() throws IOException {
		Path directory = dir.toPath();
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});
	}
}
