package org.vaadin.mideaas.frontend;

import java.util.LinkedList;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.mideaas.model.SharedProject;
import org.vaadin.mideaas.model.SharedProject.ProjectListener;
import org.vaadin.mideaas.model.User;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

// TODO

@SuppressWarnings("serial")
public class ProjectItemList extends CustomComponent implements ItemClickListener, ProjectListener {

	public interface Listener {
		public void componentSelected(String name);
		public void javaFileSelected(String name);
	}
	
	private static final String TITLE_VIEWS = "Views";
	private static final String TITLE_OTHER_FILES = "Other Files";
	
	private final User user;
	
	private LinkedList<Listener> listeners = new LinkedList<Listener>();
	
	private final SharedProject project;
	private final Tree tree = new Tree();

	private UI ui;

	
	public ProjectItemList(SharedProject proj, final User user) {
		super();
		this.project = proj;
		this.user = user;
		Panel p = new Panel("Project Contents");
		p.setHeight("200px");
		
		VerticalLayout la = new VerticalLayout();
		p.setContent(la);
		
		tree.setSelectable(false);
		
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
//		deleteButton.setWidth("100%");
		ho.setExpandRatio(addButton, 1);
		
		la.addComponent(ho);
		la.addComponent(tree);
		la.setExpandRatio(tree, 1);
		
		setCompositionRoot(p);
	}
	
	private void delete() {
		final String sel = (String) tree.getValue();
		if (TITLE_VIEWS.equals(tree.getParent(sel))) {
//			if (ProjectFileUtils.getFirstViewName().equals(sel)) {
//				Notification.show("Can't delete " +sel);
//				return;
//			}
			final String msg = "Delete View "+sel+"?";
			ConfirmDialog.show(ui, msg, msg, "Yes", "No",  new ConfirmDialog.Listener() {
				@Override
				public void onClose(ConfirmDialog d) {
					if (d.isConfirmed()) {
						project.removeView(sel, user);
					}
				}
			});
			
		}
		else if (TITLE_OTHER_FILES.equals(tree.getParent(sel))) {
			// XXX hard-code App.java, should get the name from somewhere else
			if ("App.java".equals(sel)) {
				Notification.show("Can't delete " +sel);
				return;
			}
			final String msg = "Delete File "+sel+"?";
			ConfirmDialog.show(ui, msg, msg, "Yes", "No",  new ConfirmDialog.Listener() {
				@Override
				public void onClose(ConfirmDialog d) {
					if (d.isConfirmed()) {
						project.removeFile(sel, user);
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
		
		draw();
		
		tree.addItemClickListener(this);
		tree.setImmediate(true);
		
		project.addListener(this);
	}
	
	@Override public void detach() {
		
		project.removeListener(this);
		
		super.detach();
	}
	
	private void draw() {
		tree.removeAllItems();
		
		tree.addItem(TITLE_VIEWS);
		
		for (String c : project.getViewNames()) {
			tree.addItem(c);
			tree.setItemIcon(c, Icons.APPLICATION_FORM);
			tree.setChildrenAllowed(c, false);
			tree.setParent(c, TITLE_VIEWS);
		}
		
		tree.addItem(TITLE_OTHER_FILES);
		
		for (String f : project.getFileNames()) {
			tree.addItem(f);
			tree.setItemIcon(f, f.endsWith(".java") ? Icons.DOCUMENT_ATTRIBUTE_J : Icons.DOCUMENT);
			tree.setChildrenAllowed(f, false);
			tree.setParent(f, TITLE_OTHER_FILES);
		}

		tree.expandItem(TITLE_VIEWS);
		tree.expandItem(TITLE_OTHER_FILES);
	}

	@Override
	public void itemClick(ItemClickEvent event) {
		
		if (TITLE_VIEWS.equals(tree.getParent(event.getItemId()))) {
			tree.select(event.getItemId());
			fireComponentSelected(""+event.getItemId());
		}
		else if (TITLE_OTHER_FILES.equals(tree.getParent(event.getItemId()))) {
			tree.select(event.getItemId());
			fireJavaFileSelected(""+event.getItemId());
		}
	}
	
	private void fireComponentSelected(String name) {
		for (Listener li : listeners) {
			li.componentSelected(name);
		}
	}
	
	private void fireJavaFileSelected(String name) {
		for (Listener li : listeners) {
			li.javaFileSelected(name);
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
}
