package org.vaadin.mideaas.ide;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class SimpleDocAdderComponent extends CustomComponent implements DocAdderComponent, ClickListener {

	private final TextField nameField = new TextField("Filename");
	private final Button addButton = new Button("Add");
	private DocAdder adder;
	
	public SimpleDocAdderComponent() {
		VerticalLayout la = new VerticalLayout();
		la.setMargin(true);
		la.setSpacing(true);
		la.addComponent(nameField);
		la.addComponent(addButton);
		addButton.addClickListener(this);
		setCompositionRoot(la);
	}
	
	@Override
	public void setDocAdder(DocAdder adder) {
		this.adder = adder;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		String filename = nameField.getValue();
		if (filename != null && !filename.isEmpty()) {
			adder.addDoc(filename, "");
			adder.done();
		}
	}

}
