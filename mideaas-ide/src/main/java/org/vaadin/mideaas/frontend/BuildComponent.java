package org.vaadin.mideaas.frontend;

import java.util.Arrays;
import java.util.List;

import org.vaadin.mideaas.frontend.Builder.BuildListener;
import org.vaadin.mideaas.model.User;
import org.vaadin.mideaas.model.UserSettings;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class BuildComponent extends CustomComponent implements BuildListener {

	private final Builder builder;
	private final User user;

	private final VerticalLayout layout = new VerticalLayout();
	
	private final LogView logView = new LogView();
	
	private Button buildButton = new Button("Build");
	private Button cancelButton = new Button("Cancel");
	private Button showLogButton = new ShowLogButton("Build Log", logView);
	private VerticalLayout resultLayout = new VerticalLayout();
	private Embedded loadingImg = new Embedded(null, new ThemeResource(
			"../base/common/img/loading-indicator.gif"));

	private static final List<String> GOALS_PACKAGE = Arrays
			.asList(new String[] { "vaadin:update-widgetset", "vaadin:compile", "package" });

	
//	private List<String> goals = GOALS_PACKAGE;

//	private boolean firstBuild = true;

	private UI ui;
	private final UserSettings settings;

	public BuildComponent(Builder builder, User user, UserSettings settings) {
		this.builder = builder;
		this.user = user;
		this.settings = settings;
		
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

		buildButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				logView.clear();
				build(GOALS_PACKAGE);
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
		setUi(UI.getCurrent());
		builder.addBuildListener(this);
	}
	
	@Override
	public void detach() {
		super.detach();
		builder.removeBuildListener(this);
	}
	
	private synchronized void setUi(UI ui) {
		this.ui = ui;
	}
	
	private synchronized UI getUi() {
		return ui;
	}

	public void cancelBuild() {
		builder.cancel();
	}

	private void build(List<String> goals) {
		builder.build(goals, MavenUtil.targetDirFor(user), settings.userAgent, logView);
	}


	private void finishBuild() {
		loadingImg.setVisible(false);
		
		buildButton.setEnabled(true);
		buildButton.setCaption("Build");
		cancelButton.setVisible(false);
	}

	private void buildFail(String s) {
		Label lab = new Label(s);
		lab.setIcon(Icons.CROSS_CIRCLE);
		resultLayout.addComponent(lab);
	}

	private void buildSuccess() {
		Label lab = new Label("Build successful");
		lab.setIcon(Icons.TICK_CIRCLE);
		resultLayout.addComponent(lab);
	}

	@Override
	public void buildFinished(final boolean success) {
		getUi().access(new Runnable() {
			@Override
			public void run() {
				finishBuild();
				if (success) {
					buildSuccess();
				} else {
					buildFail("Build failed.");
				}
			}
		});
	}

	@Override
	public void buildCancelled() {
		getUi().access(new Runnable() {
			@Override
			public void run() {
				finishBuild();
				buildFail("Build cancelled");
			}
		});
	}

	@Override
	public void buildStarted(List<String> goals) {
		getUi().access(new Runnable() {
			@Override
			public void run() {
				buildButton.setEnabled(false);
				resultLayout.removeAllComponents();
				cancelButton.setVisible(true);
				loadingImg.setVisible(true);
				loadingImg.setCaption("Building...");
			}
		});
		
	}

}
