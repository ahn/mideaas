package org.vaadin.mideaas.app.java.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class MemorySource extends SimpleJavaFileObject {
	private final String className;
	private final String src;

	public MemorySource(String fullClassName, String src) {
		super(URI.create("string:///" + fullClassName.replace('.', '/')+ ".java"), Kind.SOURCE);
		this.className = fullClassName;
		this.src = src;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getSource() {
		return src;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return src;
	}

	public OutputStream openOutputStream() {
		throw new IllegalStateException();
	}

	public InputStream openInputStream() {
		return new ByteArrayInputStream(src.getBytes());
	}
}