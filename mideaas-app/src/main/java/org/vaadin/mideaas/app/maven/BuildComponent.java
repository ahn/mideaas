package org.vaadin.mideaas.app.maven;

import java.util.Arrays;
import java.util.List;

import org.vaadin.mideaas.app.Icons;
import org.vaadin.mideaas.app.UserSettings;
import org.vaadin.mideaas.app.maven.Builder.BuildListener;
import org.vaadin.mideaas.app.maven.Builder.BuildStatus;
import org.vaadin.mideaas.app.maven.Builder.Status;
import org.vaadin.mideaas.ide.IdeUser;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class BuildComponent extends CustomComponent implements BuildListener {

	private final Builder builder;

	private final VerticalLayout layout = new VerticalLayout();
	
	// TODO: common log for all
	// Now, only the UI that started the build sees it.
	private final LogView logView = new LogView();
	
	private Button buildButton = new Button("Build");
	private Button cancelButton = new Button("Cancel");
	private Button showLogButton = new ShowLogButton("Build Log", logView);
	private VerticalLayout resultLayout = new VerticalLayout();
	private Embedded loadingImg = new Embedded(null, Icons.LOADING_INDICATOR);

	private static final List<String> GOALS_PACKAGE = Arrays
			.asList(new String[] { "vaadin:update-widgetset", "vaadin:compile", "package" });


	public BuildComponent(Builder builder, IdeUser user, final UserSettings userSettings) {
		this.builder = builder;
		
		Panel p = new Panel("Build");
		p.setContent(layout);
		setCompositionRoot(p);
		layout.setMargin(true);
		
		layout.addComponent(buildButton);
		
		layout.addComponent(resultLayout);
		layout.addComponent(loadingImg);
		layout.addComponent(cancelButton);
		layout.addComponent(showLogButton);
		loadingImg.setVisible(false);
		cancelButton.setVisible(false);
		layout.setExpandRatio(buildButton, 1);
		
		layout.setComponentAlignment(buildButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(loadingImg, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(cancelButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(showLogButton, Alignment.MIDDLE_CENTER);

		buildButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				logView.clear();
				build(GOALS_PACKAGE, userSettings);
			}
		});

		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				cancelBuild();
			}
		});
	}
	
	@Override
	public void attach() {
		super.attach();
		builder.addBuildListener(this);
		buildStatusChanged(builder.getStatus());
	}
	
	@Override
	public void detach() {
		super.detach();
		builder.removeBuildListener(this);
	}
	
	public void cancelBuild() {
		builder.cancel();
	}

	private void build(List<String> goals, UserSettings userSettings) {
		builder.build(goals, "target", userSettings, logView);
	}

	private void drawBuildFinished() {
		loadingImg.setVisible(false);
		buildButton.setEnabled(true);
		buildButton.setCaption("Build");
		cancelButton.setVisible(false);
	}

	private void drawBuildFailed(String msg, List<String> goals) {
		drawBuildFinished();
		Label lab = new Label(msg);
		lab.setIcon(Icons.CROSS_CIRCLE);
		lab.setSizeUndefined();
		resultLayout.addComponent(lab);
		resultLayout.setComponentAlignment(lab, Alignment.MIDDLE_CENTER);
	}

	private void drawBuildSucceeded(List<String> goals) {
		drawBuildFinished();
		Label lab = new Label("Build successful");
		lab.setIcon(Icons.TICK_CIRCLE);
		lab.setSizeUndefined();
		resultLayout.addComponent(lab);
		resultLayout.setComponentAlignment(lab, Alignment.MIDDLE_CENTER);
	}
	
	private void drawBuildRunning(List<String> goals) {
		buildButton.setEnabled(false);
		resultLayout.removeAllComponents();
		cancelButton.setVisible(true);
		loadingImg.setVisible(true);
		loadingImg.setCaption("Building...");
	}

	public void drawBuildCancelled(List<String> goals) {
		drawBuildFailed("Build cancelled", goals);
	}
	
	private void drawStatus(BuildStatus status) {
		if (status.status == Status.RUNNING) {
			drawBuildRunning(status.goals);
		} else if (status.status == Status.CANCELLED) {
			drawBuildCancelled(status.goals);
		} else if (status.status == Status.SUCCEEDED) {
			drawBuildSucceeded(status.goals);
		} else if (status.status == Status.FAILED) {
			drawBuildFailed(status.errorMessage, status.goals);
		}
	}

	@Override
	public void buildStatusChanged(final BuildStatus status) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				drawStatus(status);
			}
		});
	}

	

}
