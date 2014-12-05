package org.vaadin.mideaas.app.maven;

import java.util.List;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class BuildLogWindow extends Window {

    private VerticalLayout content = new VerticalLayout();
    private List<String> log;

    public BuildLogWindow(List<String> log) {
        super("Log");
        this.log = log;
        setWidth("90%");
        setHeight("90%");
        center();
        setContent(content);
    }
    
    @Override
    public void attach() {
        super.attach();
        for (String li : log) {
            content.addComponent(new Label(li));
        }
    }
    
}
