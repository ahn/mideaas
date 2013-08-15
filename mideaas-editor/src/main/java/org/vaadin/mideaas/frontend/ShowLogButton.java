package org.vaadin.mideaas.frontend;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class ShowLogButton extends Button implements ClickListener {
	
	private final LogView logView;
	
	public ShowLogButton(String text, LogView logView) {
		super(text);
		this.logView = logView;
		setStyleName(BaseTheme.BUTTON_LINK);
	}
	
	@Override
	public void attach() {
		super.attach();
		addClickListener(this);
	}
	
	private void openLogWindow() {
		Window w = new Window("Log");
		w.center();
		w.setWidth("80%");
		w.setHeight("80%");
		w.setContent(logView);
		logView.setSizeFull();
		UI.getCurrent().addWindow(w);
		setEnabled(false);
		w.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				ShowLogButton.this.setEnabled(true);
			}
		});
	}

	@Override
	public void buttonClick(ClickEvent event) {
		openLogWindow();
	}
}
