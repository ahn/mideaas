package org.vaadin.mideaas.app;

import java.util.Arrays;
import java.util.LinkedList;

import org.vaadin.mideaas.model.ProjectFileUtils;
import org.vaadin.mideaas.model.SharedProject;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class CreateProjectPanel.
 */
@SuppressWarnings("serial")
public class CreateProjectPanel extends Panel {

	/** The project type group. */
	//private OptionGroup projectTypeGroup = new OptionGroup("Project type:",
	//		Arrays.asList(new String[] {"Vaadin","VaadinOSGi","VaadinAppEngine", "Python", "Generic"}));

	private OptionGroup projectTypeGroup = new OptionGroup("Project type:",
			Arrays.asList(new String[] {"Vaadin"}));

	
	/** The cnp button. */
	private final Button cnpButton = new Button("Create Project");;
	
	/** The sk box. */
	private final CheckBox skBox = new CheckBox("Create Application Skeleton");
	
	/**
	 * The listener interface for receiving projectCreated events.
	 * The class that is interested in processing a projectCreated
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addProjectCreatedListener<code> method. When
	 * the projectCreated event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ProjectCreatedEvent
	 */
	public interface ProjectCreatedListener {
		
		/**
		 * Invoked when project is created.
		 *
		 * @param p the p
		 */
		public void projectCreated(SharedProject p);
	}
	
	/** The listeners. */
	private LinkedList<ProjectCreatedListener> listeners = new LinkedList<CreateProjectPanel.ProjectCreatedListener>();

	/** The ui. */
	private MideaasUI ui;
	
	/**
	 * Instantiates a new creates the project panel.
	 *
	 * @param ui the ui
	 */
	public CreateProjectPanel(MideaasUI ui) {
		super("Create New Project");
		this.setWidth("100%");
		this.ui = ui;
		init();
	}
	
	/**
	 * Inits the.
	 */
	private void init() {
		VerticalLayout la = new VerticalLayout();
		final TextField tf = new TextField("Project name: (lower-case letters and numbers)");
		tf.setImmediate(true);
		skBox.setValue(true);
		la.addComponent(tf);
		
		tf.addTextChangeListener(new TextChangeListener() {
			public void textChange(TextChangeEvent event) {
				cnpButton.setEnabled(ProjectFileUtils.isValidProjectName(event.getText()));
			}});
		
		projectTypeGroup.select("Vaadin");
		
/*		if (MideaasConfig.easiCloudsFeaturesTurnedOn()){
			la.addComponent(projectTypeGroup);
			la.addComponent(skBox);
		}*/
		
		initNewProjectButton(tf, skBox);
		cnpButton.setWidth("100%");
//		setIcon(ICON);
		la.addComponent(cnpButton);
		
		this.setContent(la);
	}
	
	/**
	 * New project.
	 *
	 * @param name the name
	 */
	private void newProject(String name) {
		if (ProjectFileUtils.isValidProjectName(name)) {
			
			// TODO: project type
//			String typeStr = (String)projectTypeGroup.getValue();
//			ProjectType type;
//			if ("Vaadin".equals(typeStr)) {
//				type = ProjectType.vaadin;
//			}
//			else if ("VaadinOSGi".equals(typeStr)) {
//				type = ProjectType.vaadinOSGi;
//			}
//			else if ("VaadinAppEngine".equals(typeStr)) {
//				type = ProjectType.vaadinAppEngine;
//			}
//			else if ("Python".equals(typeStr)) {
//				type = ProjectType.python;
//			}
//			else {
//				type = ProjectType.generic;
//			}

		//ui.newProject(name,skBox.getValue());
		ui.newProject(name,true);

		} else {
			Notification.show("Not a valid project name.");
		}


	}

	/**
	 * Inits the new project button.
	 *
	 * @param tf the tf
	 * @param skBox the sk box
	 */
	private void initNewProjectButton(final TextField tf, final CheckBox skBox) {
		cnpButton.setEnabled(false);
		cnpButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
				String name = ((String)tf.getValue()).toLowerCase();            	
            	newProject(name);
            }
        });		
	}
	
	/**
	 * Adds the listener.
	 *
	 * @param li the li
	 */
	public void addListener(ProjectCreatedListener li) {
		listeners.add(li);
	}
}
