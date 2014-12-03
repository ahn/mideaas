package org.vaadin.mideaas.app;

import org.vaadin.mideaas.ide.BuildComponent;
import org.vaadin.mideaas.ide.IdeProject;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class MockBuildComponent extends CustomComponent implements
		BuildComponent {

	private final Button button = new Button("naps");

	public MockBuildComponent(IdeProject project) {

		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				startBuild();
			}
		});

		setCompositionRoot(button);
	}

	private void startBuild() {
		button.setCaption("building...");

		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				getUI().access(new Runnable() {
					@Override
					public void run() {
						button.setCaption("built");
					}
				});
				
			}
		});
		t.start();
	}

}
