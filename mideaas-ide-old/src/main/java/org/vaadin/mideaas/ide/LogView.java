package org.vaadin.mideaas.ide;

import java.util.LinkedList;

import org.vaadin.mideaas.ide.MavenTask.LogListener;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class LogView extends CustomComponent implements LogListener {

	private final Panel logPanel = new Panel();
	private final VerticalLayout logLayout = new VerticalLayout();
	
	private LinkedList<String> logBeforeAttach = new LinkedList<String>();

	private UI ui;
	
	public LogView() {
		super();
		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		
		Button clearButton = new Button("Clear");
		logLayout.addComponent(clearButton);
		clearButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				clear();
			}
		});
		
		la.addComponent(clearButton);
		la.addComponent(logPanel);
		la.setExpandRatio(logPanel, 1);
		logPanel.setContent(logLayout);
		logPanel.setSizeFull();
		setCompositionRoot(la);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		showLog(logBeforeAttach);
		
		setUi(UI.getCurrent());
	}
	
	public void clear() {
		logBeforeAttach.clear();
		logLayout.removeAllComponents();
	}

	private void showLog(LinkedList<String> log) {
		for (String li : log) {
			showNewLogLine(li);
		}
	}
	
	private void showNewLogLine(String li) {
		Label lab;
		if (li.startsWith("[ERROR]")) {
			lab = new Label("<strong>"+li+"</strong>", ContentMode.HTML);
		}
		else {
			lab = new Label(li);
		}
		logLayout.addComponent(lab);
	}

	private synchronized UI getUi() {
		return ui;
	}
	
	private synchronized void setUi(UI ui) {
		this.ui = ui;
	}
	
	@Override
	public void newLine(final String line) {
		UI ui = getUi();
		if (ui!=null) {
			// attached
			ui.access(new Runnable() {
				@Override
				public void run() {
					showNewLogLine(line);
					logPanel.setScrollTop(999999); // ?
				}
			});
			
		}
		else {
			logBeforeAttach.add(line);
		}
	}

}
