package org.vaadin.mideaas.ide;

import java.util.Map;
import java.util.TreeMap;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ExampleProjectLobbyView extends CustomComponent implements IdeLobbyView, ClickListener {

	private final VerticalLayout layout = new VerticalLayout();
	private final Panel panel = new Panel();
	
	private void drawLayout() {
		getUI().getPage().setTitle("Welcome");
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		this.setSizeFull();
		
		panel.setCaption("Welcome");
		panel.setWidth("400px");
		panel.setHeight("400px");
		
		layout.setMargin(true);
		layout.setSpacing(true);
		panel.setContent(layout);
		
		la.addComponent(panel);
		la.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		
		setCompositionRoot(la);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		drawLayout();
		drawSelect();
	}

	private void drawSelect() {
		Button b = new Button("Open example project");
		b.addClickListener(this);
		layout.addComponent(b);		
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Map<String, String> files = getExampleProjectFiles();
		((IdeUI)getUI()).startProject("Example project", files);
	}

	private static Map<String, String> getExampleProjectFiles() {
		TreeMap<String, String> contents = new TreeMap<String, String>();
		contents.put("index.html", INDEX_HTML);
		contents.put("style.css", STYLE_CSS);
		return contents;
	}
	

	private static String INDEX_HTML = "<!DOCTYPE html>\n<html>\n    <head>\n        <title>Hello</title>\n"
			+ "        <link href=\"./style.css\" rel=\"stylesheet\" />\n    <head>\n"
			+ "    <body>\n        <p>Hello!</p>\n    </body>\n</html>\n";
	
	private static String STYLE_CSS = "body {\n    color: red;\n}\n";
	
}
