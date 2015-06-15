package org.vaadin.mideaas.app;

import org.vaadin.mideaas.ide.DocAdder;
import org.vaadin.mideaas.ide.DocAdderComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class JavaDocAdderComponent extends DocAdderComponent implements ClickListener {

	private final VaadinProject project;
	private final String pckage;
	private final TextField nameField = new TextField();
	private final Button addButton = new Button("Add");
	
	public JavaDocAdderComponent(VaadinProject project, String suggestedPackage, DocAdder docAdder) {
		super(docAdder);
		pckage = suggestedPackage;
		this.project = project;

		VerticalLayout la = new VerticalLayout();
		la.setMargin(true);
		la.setSpacing(true);
		
		la.addComponent(new Label("Class Name:"));
		
		HorizontalLayout ho = new HorizontalLayout();
		ho.addComponent(new Label(suggestedPackage+"."));
		ho.addComponent(nameField);
		la.addComponent(ho);
		
		la.addComponent(addButton);
		nameField.focus();
		addButton.addClickListener(this);
		setCompositionRoot(la);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		String cls = nameField.getValue();
		if (cls == null || cls.isEmpty()) {
			return;
		}
		String first = cls.substring(0,1);
		if (!first.equals(first.toUpperCase())) {
			return;
		}
		String filename = "src/main/java/" + pckage.replace('.', '/') + "/" + cls + ".java";
		addDoc(filename, createContent(pckage, cls));
		project.compileAll();
		done();
	}
	
	private static String createContent(String pckage, String cls) {
		return "package " + pckage + ";\n\npublic class " + cls + " {\n\n}\n";
	}

}
