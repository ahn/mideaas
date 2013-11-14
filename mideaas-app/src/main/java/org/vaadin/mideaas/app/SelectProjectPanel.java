package org.vaadin.mideaas.app;

import java.util.Collection;
import java.util.List;

import org.vaadin.mideaas.frontend.Icons;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.User;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * The Panel for selecting which project is going to be opened.
 */
@SuppressWarnings("serial")
public class SelectProjectPanel extends Panel implements
		ItemClickListener, ValueChangeListener {

	/** The ui. */
	private MideaasUI ui;
	
	/** The table for projects. */
	private Table table = new Table();
	{
		table.setWidth("100%");
		table.addContainerProperty("Project name", String.class, null);
		table.addContainerProperty("Collaborators", String.class, null);
		table.setSelectable(true);
		table.addItemClickListener(this);
		table.addValueChangeListener(this);
		table.setImmediate(true);
	}
	
	/** The button for opening the project. */
	private Button openProjectButton = new Button("Open");
	{
		openProjectButton.setWidth("100%");
		openProjectButton.setEnabled(false);
		openProjectButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				if (table.getValue() != null) {
					fireOpenEditor((String) table.getValue());
				}
			}
		});
		
	}
		
	/** The button for removing projects. */
	private Button removeProjectButton = new Button();
	{
		removeProjectButton.setEnabled(false);
		removeProjectButton.addClickListener(new ClickListener() {
		public void buttonClick(ClickEvent event) {
			if (table.getValue() != null) {
				fireRemoveRequested((String) table.getValue());
			}
		}
		});
		removeProjectButton.setIcon(Icons.CROSS_SCRIPT);
	}

	/**
	 * Instantiates a new select project panel.
 	*
 	* @param ui the ui
 	*/
	public SelectProjectPanel(MideaasUI ui) {
		super("Open Project");
		this.ui = ui;
		setIcon(Icons.BOX_ARROW);
		
		update();
	}
	
	public void update() {
		table.removeAllItems();
		
		Collection<String> projects = SharedProject.getProjectNames();
		for (String projectName : projects) {
			List<User> users = SharedProject.getProjectUsers(projectName);
			String collabStr = createCollabString(users);
			table.addItem(new Object[] { projectName, collabStr }, projectName);
		}

		// creates layouts and layouts components
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(table);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.addComponent(removeProjectButton);
		hl.addComponent(openProjectButton);
		hl.setExpandRatio(openProjectButton, 1);

		layout.addComponent(hl);
		this.setContent(layout);
	}


	private static String createCollabString(List<User> users) {
		String usersString = "";
		if (!users.isEmpty()) {
			int i = 0;
			for (User u : users) {
				String name = u.getName();
				if (i == 0) {
					// first user without starting comma
					usersString = name;
				} else if (i == users.size()-1) {
					// last user with starting " and "
					usersString = usersString + " and " + name;
				} else {
					usersString = usersString + ", " + name;
				}
				++i;
			}
		} else {
			usersString = "-";
		}
		return usersString;
	}


	/**
	 * Item on the table is clicked
	 *
	 * @param event that contains the id of clicked item
	 */
	public void itemClick(ItemClickEvent event) {
		if (event.getItemId() != null) {
			if (event.isDoubleClick()) {
				fireOpenEditor((String) event.getItemId());
			} 
		}
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Object value = event.getProperty().getValue();
		if (value!=null&&!value.equals("")){
			openProjectButton.setCaption("Open " + value);
		} else {
			openProjectButton.setCaption("");
		}
		openProjectButton.setEnabled(value!=null);
		removeProjectButton.setEnabled(value!=null);
	}


	
	/**
	 * Opens the Editor
	 *
	 * @param projectName of the project to be opened
	 */
	private void fireOpenEditor(String projectName) {
		ui.openMideaasEditor(projectName);
	}
	
	/**
	 * Removes a project.
	 *
	 * @param projectName of the project to be removed
	 */
	private void fireRemoveRequested(String projectName) {
		ui.removeProject(projectName);
	}
}
