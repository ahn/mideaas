package org.vaadin.aceeditor.java.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MyClassInfo extends MyInfo {

	private MyClassInfo parentClass = null;
	private List<MyVariableInfo> variables = new LinkedList<MyVariableInfo>();
	private List<MyVariableInfo> staticVariables = new LinkedList<MyVariableInfo>();
	private List<MyMethodInfo> staticMethods = new LinkedList<MyMethodInfo>();
	private List<MyMethodInfo> methods = new LinkedList<MyMethodInfo>();
	private List<MyClassInfo> classes = new LinkedList<MyClassInfo>();

	public MyClassInfo(String name, long startPosition, long endPosition,
			Class<?> extendsClass, MyClassInfo parentClass, boolean isStatic) {
		super(name, startPosition, endPosition, isStatic);
		setExtendsClass(extendsClass);
		this.parentClass = parentClass;
	}

	public MyClassInfo(Class<?> classi) {
		super(classi.getName(), -1, -1, java.lang.reflect.Modifier
				.isStatic(classi.getModifiers()));
		try {
			setExtendsClass(classi);
		}
		catch (NoClassDefFoundError e) {
			System.err.println("WARNING: NoClassDefFoundError for "+classi);
		}
	}

	private void setExtendsClass(Class<?> extendsClass) {
		Method[] ms = extendsClass.getMethods();
		for (Method m : ms) {
			MyMethodInfo methodInfo = new MyMethodInfo(m);
			if (methodInfo.isStatic()) {
				staticMethods.add(methodInfo);
			} else {
				methods.add(methodInfo);
			}
		}
	}

	public static MyMethodInfo findMethod(MyClassInfo c, int cursor) {
		for (MyMethodInfo m : c.methods) {
			if (cursor > m.getStartPosition() && cursor < m.getEndPosition()) {
				return m;
			}
		}
		return null;
	}

	public boolean AddClassVariable(MyVariableInfo v) {
		if (v.getStartPosition() > this.getStartPosition()
				&& v.getEndPosition() < this.getEndPosition()) {
			if (v.isStatic()) {
				this.staticVariables.add(v);
			} else {
				this.variables.add(v);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean AddMethod(MyMethodInfo v) {
		if (v.getStartPosition() > this.getStartPosition()
				&& v.getEndPosition() < this.getEndPosition()) {
			if (v.isStatic()) {
				this.staticMethods.add(v);
			} else {
				methods.add(v);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean AddClass(MyClassInfo v) {
		// checks if v belongs to the childrens of this
		if (v.getStartPosition() > this.getStartPosition()
				&& v.getEndPosition() < this.getEndPosition()) {
			// checks out if v belons to one of the nested classes
			for (MyClassInfo c : this.classes) {
				if (v.getStartPosition() > c.getStartPosition()
						&& v.getEndPosition() < c.getEndPosition()) {
					if (c.AddClass(v)) {
						return true;
					}
				}
			}
			// if not, then we add this as a parentclass and add v to the list
			classes.add(v);
			v.addParentClass(this);
			return true;
		} else {
			return false;
		}
	}

	private void addParentClass(MyClassInfo parentClass) {
		this.parentClass = parentClass;
	}

	public List<MyVariableInfo> getVariables() {
		return this.variables;
	}

	public List<MyVariableInfo> getStaticVariables() {
		return this.staticVariables;
	}

	public List<MyClassInfo> getClassNodes() {
		return this.classes;
	}

	public List<MyMethodInfo> getStaticMethods() {
		return this.staticMethods;
	}

	public MyMethodInfo getMethodUnderCursor(int cursorIndex) {
		List<MyMethodInfo> ms = new LinkedList<MyMethodInfo>();
		ms.addAll(this.staticMethods);
		ms.addAll(this.methods);

		for (MyMethodInfo m : ms) {
			long start = m.getStartPosition();
			long end = m.getEndPosition();
			if (cursorIndex >= start && cursorIndex <= end) {
				return m;
			}
		}
		return null;
	}

	public List<MyMethodInfo> getMethods() {
		return this.methods;
	}

	public MyClassInfo getParentClass() {
		return this.parentClass;
	}

	public Collection<? extends MyMethodInfo> getParentClassMethods() {
		List<MyMethodInfo> ms = new LinkedList<MyMethodInfo>();
		if (this.parentClass != null) {
			ms.addAll(this.parentClass.getStaticMethods());
			if (!this.parentClass.isStatic()) {
				ms.addAll(this.parentClass.getMethods());
			}
			ms.addAll(this.parentClass.getParentClassMethods());
		}
		return ms;
	}

	public Collection<? extends MyVariableInfo> getParentClassFields() {
		List<MyVariableInfo> vs = new LinkedList<MyVariableInfo>();
		if (this.parentClass != null) {
			vs.addAll(this.parentClass.getStaticVariables());
			if (!this.parentClass.isStatic()) {
				vs.addAll(this.parentClass.getVariables());
			}
			vs.addAll(this.parentClass.getParentClassFields());
		}
		return vs;
	}

}