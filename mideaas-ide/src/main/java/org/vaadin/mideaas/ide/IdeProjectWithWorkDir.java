package org.vaadin.mideaas.ide;

import java.io.File;

public class IdeProjectWithWorkDir extends IdeProject {

	private final IdeProjectDir workDir;

	public IdeProjectWithWorkDir(String id, String name, IdeProjectCustomizer customizer, File workDir) {
		super(id, name, customizer);
		this.workDir = new IdeProjectDir(this, workDir);
	}

	@Override
	public IdeDoc putDoc(String id, String content) {
		IdeDoc doc = super.putDoc(id, content);
		writeToDisk(); // TODO: could write exactly this file
		return doc;
	}
	
	@Override
	public IdeDoc removeDoc(String id) {
		IdeDoc doc = super.removeDoc(id);
		writeToDisk(); // TODO: could remove exactly this file
		return doc;
	}

	/**
	 * can be overridden
	 */
	@Override
	public void destroy() {
		super.destroy();
		workDir.destroy();
	}
	
	protected IdeProjectDir getWorkDir() {
		return workDir;
	}

	public void writeToDisk() {
		workDir.writeToDisk();
	}

}
