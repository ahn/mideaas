package org.vaadin.mideaas.app.maven;

import java.util.Arrays;
import java.util.List;

import org.vaadin.mideaas.app.Icons;
import org.vaadin.mideaas.app.java.MavenUtil;
import org.vaadin.mideaas.app.maven.Builder.BuildListener;
import org.vaadin.mideaas.ide.IdeUser;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
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
	private final IdeUser user;

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

	private UI ui;

	public BuildComponent(Builder builder, IdeUser user) {
		this.builder = builder;
		this.user = user;
		
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
		builder.build(goals, MavenUtil.targetDirFor(user.getId()), logView);
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
		lab.setSizeUndefined();
		resultLayout.addComponent(lab);
		resultLayout.setComponentAlignment(lab, Alignment.MIDDLE_CENTER);
	}

	private void buildSuccess() {
		Label lab = new Label("Build successful");
		lab.setIcon(Icons.TICK_CIRCLE);
		lab.setSizeUndefined();
		resultLayout.addComponent(lab);
		resultLayout.setComponentAlignment(lab, Alignment.MIDDLE_CENTER);
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
