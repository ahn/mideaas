package org.vaadin.mideaas.app.java.util;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.sun.source.tree.VariableTree;

public class MyMethodInfo extends MyInfo {
	private List<MyVariableInfo> variables = new LinkedList<MyVariableInfo>();
	private List<String> parameterTypes = new LinkedList<String>();
	private List<VariableTree> parameterTrees = new LinkedList<VariableTree>();
	private String returnTypeStr;

	public MyMethodInfo(long startPosition, long endPosition, String name,
			List<? extends VariableTree> names, String returnType,
			boolean isStatic) {
		super(name, startPosition, endPosition, isStatic);
		for (VariableTree t : names) {
			this.parameterTypes.add(t.toString());
			this.parameterTrees.add(t);
		}
		this.returnTypeStr = returnType;
	}

	public MyMethodInfo(Method m) {
		super(m.getName(), -1, -1, java.lang.reflect.Modifier.isStatic(m
				.getModifiers()));
		Class<?>[] parameters = m.getParameterTypes();
		for (Class<?> p : parameters) {
			String pn = p.getSimpleName();
			this.parameterTypes.add(pn);
		}
		this.returnTypeStr = m.getReturnType().getCanonicalName();
	}

	public boolean AddVariable(MyVariableInfo v) {
		if (v.getStartPosition() > this.getStartPosition()
				&& v.getEndPosition() < this.getEndPosition()) {
			variables.add(v);
			return true;
		} else {
			return false;
		}
	}

	public List<MyVariableInfo> getVariables() {
		return this.variables;
	}

	private String getParamString() {
		String ps = "(";
		if (parameterTypes != null) {
			int j = 0;
			// add all the parameters to string
			for (String p : parameterTypes) {
				if (j != 0) {
					ps += ", ";
				}
				ps += p;
				j++;
			}
		}
		ps += ")";
		return ps;
	}

	public List<String> getParameterTypes() {
		return this.parameterTypes;
	}

	public List<VariableTree> getParameterTrees() {
		return this.parameterTrees;
	}

	public int nmbrOfParameters() {
		if (this.parameterTypes == null) {
			return 0;
		} else {
			return this.parameterTypes.size();
		}
	}

	@Override
	public String getName() {
		return super.getName() + this.getParamString();
	}

	public String getReturnType() {
		return this.returnTypeStr;
	}
}
