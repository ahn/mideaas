package org.vaadin.mideaas.app.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * The Class ZipUtils.
 */
public class ZipUtils {

	/**
	 * Relativize.
	 *
	 * @param base the base
	 * @param child the child
	 * @return the file
	 */
	private static File relativize(File base, File child) {
		return new File(relativizePath(base, child));
	}
	
	/**
	 * Relativize path.
	 *
	 * @param base the base
	 * @param child the child
	 * @return the string
	 */
	private static String relativizePath(File base, File child) {
		return base.toURI().relativize(child.toURI()).getPath();
	}

	// http://www.java-examples.com/create-zip-file-directory-recursively-using-zipoutputstream-example
	/**
	 * Zip dir.
	 *
	 * @param dir the dir
	 * @param zipFile the zip file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void zipDir(File dir, File zipFile) throws IOException {
		FileOutputStream fout = new FileOutputStream(zipFile);
		ZipOutputStream zout = new ZipOutputStream(fout);
		File base=dir.getParentFile();
		addDirToZip(base, dir, zout);
		zout.close();
	}

	/**
	 * Adds the dir to zip.
	 *
	 * @param base the base
	 * @param dir the dir
	 * @param zout the zout
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void addDirToZip(File base, File dir, ZipOutputStream zout)
			throws IOException {
//		zout.putNextEntry(new ZipEntry(ZipUtils.relativize(base,dir).getPath()+ "/"));
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				zout.putNextEntry(new ZipEntry(ZipUtils.relativize(base,
						file).getPath()
						+ "/"));
				addDirToZip(base, file, zout);
			} else {
				byte[] buffer = new byte[1024];
				FileInputStream fin = new FileInputStream(file);
				zout.putNextEntry(new ZipEntry(ZipUtils.relativize(base,
						file).getPath()));
				int length;
				while ((length = fin.read(buffer)) > 0) {
					zout.write(buffer, 0, length);
				}
				zout.closeEntry();
				fin.close();
			}
		}
	}

	/**
	 * Copy.
	 *
	 * @param in the in
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	/**
	 * Copy.
	 *
	 * @param in the in
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void copy(InputStream in, File file) throws IOException {
		file.getParentFile().mkdirs();
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}	
	
	// http://huljas.github.com/code/2012/03/30/little-unzip-utility.html
	/**
	 * Unzip.
	 *
	 * @param zipFile the zip file
	 * @param targetDir the target dir
	 * @return the root directory of project
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File unzip(File zipFile, File targetDir)
			throws IOException {
		File projectRootDirectory = null;
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
			ArrayList<? extends ZipEntry> entries = Collections.list(zip.entries());
			for (ZipEntry entry : entries) {
				InputStream input = zip.getInputStream(entry);
				try {
					if (!targetDir.exists())
						targetDir.mkdirs();
					File target = new File(targetDir, entry.getName());
					if (entry.isDirectory()) {
						target.mkdirs();
						//the following is done only once
						if (projectRootDirectory==null){
							//target is one of the files inside the project and therefore by searching from the parents we can find the projectDirectory
							projectRootDirectory = target;
							while(!targetDir.equals(projectRootDirectory.getParentFile())){
								projectRootDirectory=projectRootDirectory.getParentFile();
							}
						}
					} else {
						copy(input, target);
					}
				} finally {
					input.close();
				}
			}
		} finally {
			zip.close();
		}
		return projectRootDirectory;
	}
	
	// XXX copy-pasting :(
	public static String projectNameInZip(File zipFile)
			throws IOException {
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
			ArrayList<? extends ZipEntry> entries = Collections.list(zip.entries());
			for (ZipEntry entry : entries) {
				InputStream input = zip.getInputStream(entry);
				try {
					File target = new File(entry.getName());
					while(target.getParentFile()!=null){
						target=target.getParentFile();
					}
					return target.getName();
				} finally {
					input.close();
				}
			}
		} finally {
			zip.close();
		}
		return null; // ?
	}

	// http://stackoverflow.com/questions/617414/create-a-temporary-directory-in-java
	/**
	 * Creates the temp directory.
	 *
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File createTempDirectory() throws IOException {
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: "
					+ temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: "
					+ temp.getAbsolutePath());
		}

		return temp;
	}
}
