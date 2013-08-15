package org.vaadin.mideaas.app;

import java.util.HashMap;
import java.util.Map.Entry;

import org.vaadin.mideaas.model.UserSettings;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class WidgetsetSettingsWindow extends Window {

	private final UserSettings settings;

	// https://code.google.com/p/google-web-toolkit/source/browse/trunk/user/src/com/google/gwt/useragent/UserAgent.gwt.xml
	private static final String[] AGENTS = new String[] { "ie6", "ie8", "ie9",
			"gecko1_8", "safari", "opera", };

	public WidgetsetSettingsWindow(UserSettings settings) {
		this.settings = settings;
	}

	@Override
	public void attach() {
		super.attach();

		VerticalLayout ve = new VerticalLayout();
		setContent(ve);
		ve.addComponent(new Label(
				"Select user agents for which to build the GWT widgetset:"));

		final HashMap<String, CheckBox> boxes = new HashMap<String, CheckBox>();
		for (String a : AGENTS) {
			CheckBox cb = new CheckBox(a);
			ve.addComponent(cb);
			boxes.put(a, cb);
		}

		if (settings.userAgent != null) {
			String[] sel = settings.userAgent.split(",");
			for (String a : sel) {
				boxes.get(a.trim()).setValue(true);
			}
		}

		Button b = new Button("Done");
		b.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String s = null;
				for (Entry<String, CheckBox> e : boxes.entrySet()) {
					if (e.getValue().getValue()) {
						if (s == null) {
							s = e.getKey();
						} else {
							s += "," + e.getKey();
						}
					}
				}
				settings.userAgent = s;
				close();
			}
		});
		ve.addComponent(b);

		center();
		setWidth("60%");
		setHeight("60%");
	}

}
