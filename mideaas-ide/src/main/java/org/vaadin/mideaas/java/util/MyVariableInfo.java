package org.vaadin.mideaas.java.util;

import com.sun.source.tree.Tree;

public class MyVariableInfo extends MyInfo {
	private Tree type;

	public MyVariableInfo(String name, Tree type, long startPosition,
			long endPosition, boolean isStatic) {
		super(name, startPosition, endPosition, isStatic);
		this.type = type;
	}

	public String getClassName() {
		return type.toString();
	}
}
