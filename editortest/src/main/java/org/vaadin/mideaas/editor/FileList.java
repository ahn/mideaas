package org.vaadin.mideaas.editor;

import java.util.Collection;
import java.util.LinkedList;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class FileList extends CustomComponent implements MultiUserProject.Listener, ValueChangeListener, ItemClickListener {

	
	private static final String LIST_HEADER = "Files";


	public interface Listener {
		public void selected(String name);
	}

	private LinkedList<Listener> listeners = new LinkedList<Listener>();
	
	private final MultiUserProject project;
	private final Table table;
	private final Link link;
	
	
	public FileList(MultiUserProject project) {
		this.project = project;
		table = new Table();
		table.setSizeFull();
		table.setPageLength(10);
		table.addContainerProperty(LIST_HEADER, String.class, null);
		table.setSelectable(true);
		table.addItemClickListener(this);
		table.addValueChangeListener(this);
		
		link = new Link("Open in a new tab.", null);
		link.setEnabled(false);
		
		VerticalLayout la = new VerticalLayout();
		la.addComponent(table);
		la.addComponent(link);
		
		setCompositionRoot(la);
	}
	
	public void addListener(Listener li) {
		listeners.add(li);
	}
	
	public void removeListener(Listener li) {
		listeners.remove(li);
	}
	
	
	@Override
	public void attach() {
		super.attach();
		
		updateTable(project.getDocNames());
		
		project.addListener(this);
	}
	



	@Override
	public void detach() {
		
		project.removeListener(this);
		
		super.detach();
		
		
	}

	
	
	private void updateTable(Collection<String> names) {
		table.removeAllItems();
		
		System.out.println(names);
		for (String name : names) {
			table.addItem(new Object[]{name}, name);
		}
	}


	@Override
	public void changed(final Collection<String> docNames) {
		UI ui = getUI();
		if (ui == null) {
			return;
		}
		ui.access(new Runnable() {

			@Override
			public void run() {
				if (isAttached()) {
					updateTable(docNames);
				}
			}
			
		});
		
		
	}


	@Override
	public void valueChange(ValueChangeEvent event) {
		
		String file = (String) event.getProperty().getValue();
		
		if (file != null) {
			link.setEnabled(true);
			link.setCaption("Open " + file + " in a new tab.");
			link.setTargetName("_blank");
			link.setResource(new ExternalResource("#!"+project.getName()+"/"+file));
			fireFileSelected(file);
		}
		else {
			link.setEnabled(false);
			link.setCaption("Open in a new tab.");
		}
	}


	@Override
	public void itemClick(ItemClickEvent event) {
		if (event.isDoubleClick()) {
			//fireFileSelected((String) event.getItemId());
		}
		
	}


	private void fireFileSelected(String name) {
		for (Listener li : listeners) {
			li.selected(name);
		}
	}
	
}
