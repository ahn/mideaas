package org.vaadin.mideaas.ide;

import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public abstract class DocAdderComponent extends CustomComponent {
	
	private final DocAdder adder;
	
	public DocAdderComponent(DocAdder adder) {
		this.adder = adder;
	}
	
	protected final void addDoc(String filename, String content) {
		adder.addDoc(filename, content);
	}
	
	protected final void done() {
		adder.done();
	}
}
