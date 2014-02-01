package org.vaadin.mideaas.frontend;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.mideaas.model.ProjectItem;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.SharedProject.ProjectListener;
import org.vaadin.mideaas.model.SharedView;
import org.vaadin.mideaas.model.User;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

// TODO

@SuppressWarnings("serial")
public class ProjectItemList extends CustomComponent implements ProjectListener, ValueChangeListener {

	public interface Listener {
		public void projectItemSelected(String name);
	}
	
	private static final String TITLE_VIEWS = "Views";
	private static final String TITLE_OTHER_FILES = "Other Files";
	
	private final User user;
	
	private LinkedList<Listener> listeners = new LinkedList<Listener>();
	
	private final SharedProject project;
	private final TreeTable tree = new TreeTable();
	
	private UI ui;

	private String selected = "App.java";
	
	public ProjectItemList(SharedProject proj, final User user) {
		super();
		this.project = proj;
		this.user = user;
		VerticalLayout mainLayout = new VerticalLayout();
		Panel p = new Panel("Project Contents");
		mainLayout.addComponent(p);
		
		VerticalLayout la = new VerticalLayout();
		p.setContent(la);
		
		la.addComponent(tree);
		la.setExpandRatio(tree, 1);
		
		HorizontalLayout ho = new HorizontalLayout();
		ho.setMargin(true);
		ho.setSizeFull();
		
		Button addButton = new Button("Add New");
		addButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ui.addWindow(new AddNewWindow(project, user));
			}
		});
		
		Button deleteButton = new Button("Del");
		deleteButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				delete();
			}
		});
		
		ho.setSpacing(true);
		ho.addComponent(addButton);
		ho.addComponent(deleteButton);
		addButton.setWidth("100%");
		addButton.setIcon(Icons.PLUS);
		deleteButton.setIcon(Icons.CROSS);
		ho.setExpandRatio(addButton, 1);
		
		mainLayout.addComponent(ho);
		
		setCompositionRoot(mainLayout);
	}
	
	private void delete() {
		final String sel = (String) tree.getValue();
		if (TITLE_VIEWS.equals(tree.getParent(sel))) {
			final String msg = "Delete View "+sel+"?";
			ConfirmDialog.show(ui, msg, msg, "Yes", "No",  new ConfirmDialog.Listener() {
				@Override
				public void onClose(ConfirmDialog d) {
					if (d.isConfirmed()) {
						project.removeProjectItem(sel, user);
					}
				}
			});
			
		}
		else if (TITLE_OTHER_FILES.equals(tree.getParent(sel))) {
			// TODO hard-code App.java, should get the name from somewhere else
			if ("App.java".equals(sel)) {
				Notification.show("Can't delete " +sel);
				return;
			}
			final String msg = "Delete File "+sel+"?";
			ConfirmDialog.show(ui, msg, msg, "Yes", "No",  new ConfirmDialog.Listener() {
				@Override
				public void onClose(ConfirmDialog d) {
					if (d.isConfirmed()) {
						project.removeProjectItem(sel, user);
					}
				}
			});
		}
	}

	public void addComponentListener(Listener li) {
		listeners.add(li);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		this.ui = UI.getCurrent();
		
		
		
		tree.addContainerProperty("Name", Label.class,  null);
		tree.setImmediate(true);
		tree.setWidth("100%");
		tree.setSelectable(true);
		
		tree.addValueChangeListener(this);
		
		draw();
		
		project.addListener(this);
	}
	
	@Override
	public void detach() {
		
		project.removeListener(this);
		
		super.detach();
	}
	
	private void draw() {
		tree.removeAllItems();
		
		tree.addItem(new Object[]{new Label(TITLE_VIEWS)}, TITLE_VIEWS);
		tree.addItem(new Object[]{new Label(TITLE_OTHER_FILES)}, TITLE_OTHER_FILES);
		
		List<ProjectItem> items = project.getProjectItemsCopy();
		
		for (ProjectItem pi : items) {
			String id = pi.getName();
			tree.addItem(new Object[]{new ProjectItemLabel(pi)}, id);
			tree.setItemIcon(id, pi.getIcon());
			tree.setChildrenAllowed(id, false);
			if (pi instanceof SharedView) {
				tree.setParent(id, TITLE_VIEWS);
			}
			else {
				tree.setParent(id, TITLE_OTHER_FILES);
			}
		}

		tree.setCollapsed(TITLE_VIEWS, false);
		tree.setCollapsed(TITLE_OTHER_FILES, false);
		//tree.expandItem(TITLE_VIEWS);
		//tree.expandItem(TITLE_OTHER_FILES);
	}

	
	private void fireComponentSelected(String name) {
		for (Listener li : listeners) {
			li.projectItemSelected(name);
		}
	}
	


	@Override
	public void changed() {
		ui.access(new Runnable() {
			@Override
			public void run() {
				draw();
			}
		});
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		String id = (String) event.getProperty().getValue();
		Object parent = tree.getParent(id);
		if (TITLE_VIEWS.equals(parent) || TITLE_OTHER_FILES.equals(parent)) {
			selected = id;
			fireComponentSelected(id);
		}
	}
}
