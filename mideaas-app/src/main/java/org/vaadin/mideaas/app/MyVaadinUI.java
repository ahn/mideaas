package org.vaadin.mideaas.app;

import javax.servlet.annotation.WebServlet;

import org.vaadin.mideaas.ide.IdeCustomizer;
import org.vaadin.mideaas.ide.IdeUI;
import org.vaadin.mideaas.ide.ProjectCustomizer;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;


@SuppressWarnings("serial")
public class MyVaadinUI extends IdeUI
{

	@WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "org.vaadin.mideaas.app.AppWidgetSet")
    public static class Servlet extends VaadinServlet {}
	
	@Override
	public void init(VaadinRequest request) {
		System.out.println("jee\n\n\n\n");
		super.init(request);
	}
	
	@Override
	protected ProjectCustomizer createProjectCustomizer() {
		return new MyCustomizer();
	}
	
	@Override
	protected IdeCustomizer createIdeCustomizer() {
		return new MyIdeCustomizer();
	}

}
