package org.vaadin.mideaas.app;

import javax.servlet.annotation.WebServlet;

import org.vaadin.mideaas.ide.IdeConfiguration;
import org.vaadin.mideaas.ide.IdeUI;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

@Theme("mideaas")
@SuppressWarnings("serial")
public class MideaasUI extends IdeUI
{

	@WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MideaasUI.class, widgetset = "org.vaadin.mideaas.app.AppWidgetSet")
    public static class Servlet extends VaadinServlet {}
	
	public MideaasUI() {
		super(createConfig());
	}
	
	private static IdeConfiguration createConfig() {
		return new MideaasIdeConfiguration();
	}

}
