package org.vaadin.mideaas.editortest;

import javax.servlet.annotation.WebServlet;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.DocManager;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.MultiUserEditor;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("mytheme")
@SuppressWarnings("serial")
@Push
public class MyVaadinUI extends UI
{
	private static long lastUserId = 0;
	synchronized private static EditorUser createUser() {
		long uid = ++lastUserId;
		return new EditorUser(""+uid,"User "+uid);
	}

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "org.vaadin.mideaas.editortest.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }
    
    private static final String JAVA_CODE =
    		"package foo.bar.baz;\n\n"
    		+ "class Foo {\n\n"
    		+ "    void bar() {\n        int i = 2;\n    }\n\n"
    		+ "}\n";
    
    private static final DocManager mud = new DocManager(new AceDoc("//"));

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        setContent(layout);

        EditorUser user = createUser();
        
        Page.getCurrent().setTitle(user.getName());
        layout.addComponent(new Label("My name is "+user.getName()));
        
        MultiUserEditor mue = new MultiUserEditor(user, mud);
        mue.setSizeFull();
        mue.setMode(AceMode.java);
        
        layout.addComponent(mue);

        layout.setExpandRatio(mue, 1);
    }

}
