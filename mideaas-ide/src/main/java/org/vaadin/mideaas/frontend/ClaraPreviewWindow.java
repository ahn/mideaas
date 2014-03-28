package org.vaadin.mideaas.frontend;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ClaraPreviewWindow extends Window {

	public ClaraPreviewWindow() {
		super("Preview");
		setWidth("50%");
		setHeight("50%");
		center();
		
		// Making sure preview has touchkit style
        // XXX: ???
        addStyleName("v-tk");
	}
	
	public void setPreviewComponent(Component content) {
		content.setSizeFull(); // ?
		setContent(content);
	}
	
	public void setError(Exception e) {
		setContent(new Label("Clara was unable to build UI: " + e.getMessage()));
	}
	
	
}
