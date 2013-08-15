package org.vaadin.aceeditor.java.util;

import java.util.HashMap;
import java.util.Map;


public class SpecialClassLoader extends ClassLoader {
	private final Map<String, MemoryByteCode> m;

	public SpecialClassLoader(ClassLoader parent) {
		super(parent);
		m = new HashMap<String, MemoryByteCode>();
	}

	public SpecialClassLoader(ClassLoader parent, SpecialClassLoader xcl) {
		super(parent);
		// XXX is this ok? :p
		m = new HashMap<String, MemoryByteCode>(xcl.m);
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
//		debugPrint();
		if (m.containsKey(name)) {
			MemoryByteCode mbc = m.get(name);
			return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
		}
		else {
			throw new ClassNotFoundException(name);
		}
	}
	
	public void addClass(String fullJavaClassName, MemoryByteCode mbc) {
		m.put(fullJavaClassName, mbc);
	}
	
	public void removeClass(String fullJavaClassName) {
		m.remove(fullJavaClassName);
	}
	
	public void debugPrint() {
		System.err.println("SpecialClassLoader has " + m.size() + " items:");
		for (String x : m.keySet()) {
			System.err.println("    " + x);
		}
	}


}