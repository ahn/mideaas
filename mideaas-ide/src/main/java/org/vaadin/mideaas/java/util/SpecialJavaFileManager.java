package org.vaadin.mideaas.java.util;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

class SpecialJavaFileManager extends
		ForwardingJavaFileManager<StandardJavaFileManager> {
	private SpecialClassLoader xcl;

	public SpecialJavaFileManager(StandardJavaFileManager sjfm,
			SpecialClassLoader xcl) {
		super(sjfm);
		this.xcl = xcl;
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className,
			JavaFileObject.Kind kind, FileObject sibling) throws IOException {
		MemoryByteCode mbc = new MemoryByteCode(className);
		xcl.addClass(className, mbc);
		return mbc;
	}
}