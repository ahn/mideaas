package org.vaadin.aceeditor.java.util;

public class MyInfo implements Comparable<MyInfo> {
	private String name;
	private boolean isStatic;
	private long endPostioin;
	private long startPosition;

	public MyInfo(String name, long startPosition, long endPosition,
			boolean isStatic) {
		this.name = name;
		this.startPosition = startPosition;
		this.endPostioin = endPosition;
		this.isStatic = isStatic;
	}

	public long getEndPosition() {
		return this.endPostioin;
	}

	public long getStartPosition() {
		return this.startPosition;
	}

	public long getLenght() {
		return this.endPostioin - this.startPosition;
	}

	public String getName() {
		return this.name;
	}

	public Boolean isStatic() {
		return isStatic;
	}

	public int compareTo(MyInfo o) {
		return this.getName().compareTo(o.getName());
	}

	public boolean equals(MyInfo other) {
		return other.getName().equals(this.getName());
	}

	public int hashCode() {
		return this.getName().hashCode();
	}
}
