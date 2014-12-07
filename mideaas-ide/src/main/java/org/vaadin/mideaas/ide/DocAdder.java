package org.vaadin.mideaas.ide;

public interface DocAdder {
	public boolean addDoc(String filename, String content);
	public void done();
}
