package org.vaadin.aceeditor.java.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

public class SourceScanner extends TreePathScanner<Object, Trees> {
	private final CompilationUnitTree compilationUnitTree;
	private final SourcePositions sourcePositions;
	private List<MyClassInfo> classes = new LinkedList<MyClassInfo>();
	private List<ImportTree> imports;

	private String superClassName;
	private final InMemoryCompiler compiler;

	public SourceScanner(InMemoryCompiler compiler,
			CompilationUnitTree compilationUnitTree,
			SourcePositions sourcePosition) {
		this.compiler = compiler;
		this.compilationUnitTree = compilationUnitTree;
		this.sourcePositions = sourcePosition;
		this.imports = new LinkedList<ImportTree>(compilationUnitTree.getImports());
	}

	public List<ImportTree> getImports() {
		return this.imports;
	}

	public Object visitClass(ClassTree classTree, Trees trees) {
		long startPosition = sourcePositions.getStartPosition(
				compilationUnitTree, classTree);
		long endPosition = sourcePositions.getEndPosition(compilationUnitTree,
				classTree);
		String name = classTree.getSimpleName().toString();
		if (!name.equals("<error>")) {
			Tree extendsClassTree = classTree.getExtendsClause();
			if (extendsClassTree != null) {
				superClassName = extendsClassTree.toString();
			} else {
				superClassName = "java.lang.Object";
			}
			Class<?> extendsClass;
			try {
				extendsClass = getClassByName(superClassName);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
				extendsClass = null;
			}

			boolean isStatic = false;
			try {
				Set<javax.lang.model.element.Modifier> flags = classTree
						.getModifiers().getFlags();
				isStatic = isStatic(flags);
			} catch (Exception e) {
				e.printStackTrace();
			}

			MyClassInfo newClass = new MyClassInfo(name, startPosition,
					endPosition, extendsClass, null, isStatic);
			
			if (name.toString().trim().length() == 0) { // anonymous class?
				// ?
			}
			if (classes.isEmpty()) {
				classes.add(newClass);
			} else {
				// add new class as a subclass
				if (!classes.get(classes.size() - 1).AddClass(newClass)) {
					// or if not possible, then as a new base class
					classes.add(newClass);
				}
			}
		}
		return super.visitClass(classTree, trees);
	}

	public Object visitMethod(MethodTree methodTree, Trees trees) {
		long startPosition = sourcePositions.getStartPosition(
				compilationUnitTree, methodTree);
		long endPosition = sourcePositions.getEndPosition(compilationUnitTree,
				methodTree);
		MyClassInfo c = getClassUnderCursor((int) startPosition);

		String name = methodTree.getName().toString();
		String returnType = (methodTree.getReturnType() != null) ? methodTree
				.getReturnType().toString() : "";
		if (!name.equals("<error>")) {
			List<? extends VariableTree> names = methodTree.getParameters();
			ModifiersTree modifiers = methodTree.getModifiers();
			Set<javax.lang.model.element.Modifier> flags = modifiers.getFlags();
			c.AddMethod(new MyMethodInfo(startPosition, endPosition, name,
					names, returnType, isStatic(flags)));
		}
		return super.visitMethod(methodTree, trees);
	}

	public Object visitVariable(VariableTree variableTree, Trees trees) {
		String name = variableTree.getName().toString();
		if (!name.equals("<error>")) {
			Tree type = variableTree.getType();

			long startPosition = sourcePositions.getStartPosition(
					compilationUnitTree, variableTree);
			long endPosition = sourcePositions.getEndPosition(
					compilationUnitTree, variableTree);

			ModifiersTree modifiers = variableTree.getModifiers();
			Set<javax.lang.model.element.Modifier> flags = modifiers.getFlags();

			MyVariableInfo v = new MyVariableInfo(name, type, startPosition,
					endPosition, isStatic(flags));
			// find right method for the variable
			MyClassInfo c = getClassUnderCursor((int) startPosition);
			List<MyMethodInfo> methods = c.getMethods();

			// adds variable to right place
			boolean ok = false;
			// checks if it belongs to one of the methods
			for (MyMethodInfo method : methods) {
				if (ok) {
					break;
				} else {
					ok = method.AddVariable(v);
				}
			}
			// if variable is not inside method, then it is a field
			if (!ok) {
				c.AddClassVariable(v);
			}
		}
		return super.visitVariable(variableTree, trees);
	}

	private boolean isStatic(Set<javax.lang.model.element.Modifier> flags) {
		for (javax.lang.model.element.Modifier modifier : flags) {
			if (modifier.toString().equals("static")) {
				return true;
			}
		}
		return false;
	}

	public MyClassInfo getMyClassInfo(String className) {
		try {
			// class is one of classes written by the developer
			MyClassInfo c = getClassInfo(className, this.classes);
			if (c != null) {
				return c;
			} else {
				// class comes from libraries
				return new MyClassInfo(getClassByName(className));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private MyClassInfo getClassInfo(String className, List<MyClassInfo> mci) {
		for (MyClassInfo c : mci) {
			if (c.getName().equals(className)) {
				return c;
			}
			MyClassInfo innerC = getClassInfo(className, c.getClassNodes());
			if (innerC != null) {
				return innerC;
			}
		}
		return null;
	}

	public MyClassInfo getClassUnderCursor(int cursorIndex) {
		return findClass(this.classes, cursorIndex);
	}

	// finds recursively the nested classes
	private MyClassInfo findClass(List<MyClassInfo> cl, long cursor) {
		for (MyClassInfo c : cl) {
			if (cursor > c.getStartPosition() && cursor < c.getEndPosition()) {
				List<MyClassInfo> nodes = c.getClassNodes();
				MyClassInfo c2 = findClass(nodes, cursor);
				if (c2 != null) {
					return c2;
				} else {
					return c;
				}
			}
		}
		return null;
	}
	
	private List<String> getImportNames() {
		List<String> importNames = new LinkedList<String>();
		for (ImportTree imp : imports) {
			Tree identifier = imp.getQualifiedIdentifier();
			importNames.add(identifier.toString());
		}
		return importNames;
	}
	
	public List<MyVariableInfo> collectVariableNodesInScope(int cursorLocation, String type) {
		MyClassInfo c = getClassUnderCursor(cursorLocation);
		if (c == null) {
			return Collections.emptyList();
		}
		List<MyVariableInfo> nodes = c.getVariables();
		// add variables inside a method
		if (!type.startsWith("this")) {
			nodes.addAll(getVariableNodes(cursorLocation));
		}
		return nodes;
	}
	
	public Collection<? extends MyVariableInfo> getVariableNodes(int cursorLocation) {
		MyClassInfo c = getClassUnderCursor(cursorLocation);

		List<MyMethodInfo> methodNodes = c.getMethods();
		for (MyMethodInfo i : methodNodes) {
			if (cursorLocation > i.getStartPosition()
					&& cursorLocation < i.getEndPosition()) {
				return i.getVariables();
			}
		}
		return Collections.emptyList();
	}
	
	private Class<?> getClassByName(String className) throws ClassNotFoundException {
		String currentPackage = compilationUnitTree.getPackageName().toString();
//		throw new RuntimeException("Not implemented"); // TODO
		return compiler.getClassByName(className, currentPackage, getImportNames());
	}
}
