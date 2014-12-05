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
	
	private final UserSettings userSettings = MideaasConfig.getDefaultUserSettings();
	
	public MideaasUI() {
		super(createConfig());
//		super(new DefaultIdeConfiguration());
		
		System.out.println("ccc " + 	MideaasConfig.getFeedbackFile());
	}
	
	private static IdeConfiguration createConfig() {
		return new MideaasIdeConfiguration();
	}
	
	public UserSettings getUserSettings() {
		return userSettings;
	}

}
