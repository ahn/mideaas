package org.vaadin.mideaas.editortest;

import javax.servlet.annotation.WebServlet;

import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.JavaSyntaxErrorChecker;
import org.vaadin.mideaas.editor.MultiUserDoc;
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
	synchronized private static String createUserId() {
		return "User"+(++lastUserId);
	}

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "org.vaadin.mideaas.editortest.AppWidgetSet", heartbeatInterval=2)
    public static class Servlet extends VaadinServlet {
    }
    
    private static final String JAVA_CODE =
    		"package foo.bar.baz;\n\n"
    		+ "class Foo {\n\n"
    		+ "    void bar() {\n        int i = 2;\n    }\n\n"
    		+ "}\n";
    
    private static final MultiUserDoc mud = new MultiUserDoc(
    		"hehe", new AceDoc(JAVA_CODE), new JavaSyntaxErrorChecker(), null);

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        setContent(layout);

        String uid = createUserId();
        
        Page.getCurrent().setTitle(uid);
        layout.addComponent(new Label("My name is "+uid));
        
        MultiUserEditor mue = new MultiUserEditor(uid, mud);
        mue.setSizeFull();
        mue.setMode(AceMode.java);
        
        layout.addComponent(mue);

        layout.setExpandRatio(mue, 1);
    }

}
