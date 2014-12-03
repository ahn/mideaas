package org.vaadin.mideaas.ide.java.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ClassInfoStorage {
	
	private final static Pattern anonymousClassname = Pattern.compile("\\w+\\$\\d+");
	
	// Package names by short class name.
	private TreeMap<String, TreeSet<String>> classPackages = new TreeMap<String, TreeSet<String>>();
	
	private TreeMap<String, String> lower = new TreeMap<String, String>();
	
	private TreeSet<String> fullJavaClasses = new TreeSet<String>();
	
	private HashSet<String> classes = new HashSet<String>();
	
	public ClassInfoStorage() {
		
	}
	
	public void addClasspathItem(String classpathItem) {
		File f = new File(classpathItem);
		if (classpathItem.endsWith(".jar")) {
			walkZip(f);
		} else if (f.isDirectory()) {
			walkDir(f, null);
		}
		else {
			System.err.println("WARNING: "+classpathItem+" is neither a jar nor directory");
		}
	}

	/**
	 * Every line in the file must be format:
	 * PACKAGENAME CLASSNAME
	 * Example:
	 * java.util HashMap
	 */
	public void loadClassesFromClasspathFile(String filename) {
		InputStream is = InMemoryCompiler.class.getClassLoader().getResourceAsStream(filename);
		if (is==null) {
			System.err.println("WARNING: could not read "+filename);
			return;
		}
		Scanner s = new Scanner(is);
		s.useDelimiter("\n");
		while (s.hasNext()) {
			String lis[] = s.next().split(" ", 2);
			add(lis[1].trim(), lis[0].trim());
		
		}
		s.close();
	}
	
	public Collection<String> getClassNameStartingWith(String s) {
		return classPackages.subMap(s, s+"zzz").keySet();

	}

	private void walkZip(File jarFile) {
		try {
			FileInputStream fis = new FileInputStream(jarFile);
			ZipInputStream zip = new ZipInputStream(fis);
			ZipEntry ze = null;
			while ((ze = zip.getNextEntry()) != null) {
				String entryName = ze.getName();
				if (entryName.endsWith(".class")) {
					int es = entryName.lastIndexOf("/");
					if (es == -1) {
						break; // XXX: is this correct?
					}
					String pack = entryName.substring(0, es).replace("/", ".");
					String name = entryName.substring(es + 1,
							entryName.length() - 6);
					add(name, pack);
				}
			}
			zip.close();
		} catch (IOException e) {
			System.err.println("WARNING: failed to read jar: "+ jarFile);
		}
	}

	private void walkDir(File dir, String packageName) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				if (packageName==null) {
					walkDir(f, f.getName());
				}
				else {
					walkDir(f, packageName+"."+f.getName());
				}
			} else {
				if (f.getName().endsWith(".class")) {
					add(f.getName().substring(0,f.getName().length()-6), packageName);
				}
				else if (f.getName().endsWith(".java")) {
					add(f.getName().substring(0,f.getName().length()-5), packageName);
				}
			}
		}
	}

	private void add(String className, String packageName) {
		if (anonymousClassname.matcher(className).matches()) {
			return;
		}
		className = className.replace('$', '.');
		
		fullJavaClasses.add(packageName+"."+className);
		
		if (classPackages.containsKey(className)) {
			classPackages.get(className).add(packageName);
		} else {
			TreeSet<String> ps = new TreeSet<String>();
			ps.add(packageName);
			classPackages.put(className, ps);
		}
		
		// It's theoretically possible that two different names are same in lower case,
		// but that's too bad for them...
		lower.put(className.toLowerCase(), className);
	}
	
	private void remove(String className, String packageName) {
		fullJavaClasses.remove(packageName+"."+className);
		TreeSet<String> packs = classPackages.get(className);
		if (packs!=null) {
			packs.remove(packageName);
			if (packs.isEmpty()) {
				classPackages.remove(className);
			}
		}
	}

	public Collection<String> getPotentialPackagesForClass(String cls) {
		Collection<String> packages = classPackages.get(cls);
		if (packages==null) {
			return Collections.emptyList();
		}
		return packages;
	}

	public Collection<String> getFullClassNameStartingWith(String part) {
		return fullJavaClasses.subSet(part, part+"zzz");
	}

	public void addClass(String fullJavaClassName) {
		classes.add(fullJavaClassName);
		int lastDot = fullJavaClassName.lastIndexOf('.');
		String pack = fullJavaClassName.substring(0,lastDot);
		String cls = fullJavaClassName.substring(lastDot+1);
		add(cls, pack);
	}
	
	public void removeClass(String fullJavaClassName) {
		classes.remove(fullJavaClassName);
		int lastDot = fullJavaClassName.lastIndexOf('.');
		String pack = fullJavaClassName.substring(0,lastDot);
		String cls = fullJavaClassName.substring(lastDot+1);
		remove(cls, pack);
	}

}
