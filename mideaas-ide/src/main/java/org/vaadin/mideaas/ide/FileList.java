package org.vaadin.mideaas.ide;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class FileList extends CustomComponent implements
		IdeProject.Listener, ValueChangeListener, ItemClickListener {

	private static final String LIST_HEADER = "Files";

	public interface Listener {
		public void selected(String name);
		public void selectedNewTab(String name);
	}

	private LinkedList<Listener> listeners = new LinkedList<Listener>();

	private final IdeProject project;
	private final Tree tree;

	public FileList(IdeProject project) {
		this.project = project;
		tree = new Tree();
		tree.setSizeFull();
		//table.setPageLength(10);
		//table.addContainerProperty(LIST_HEADER, String.class, null);
		tree.setSelectable(true);
		tree.addItemClickListener(this);
		tree.addValueChangeListener(this);

		VerticalLayout la = new VerticalLayout();
		la.addComponent(tree);
		la.setSizeFull();
		
		Panel pa = new Panel(LIST_HEADER);
		pa.setContent(la);
		pa.setHeight("200px");
		pa.setWidth("100%");

		setCompositionRoot(pa);
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
		tree.removeAllItems();
		
		Map<String, Collection<String>> dirFiles = getFilesByDir(names);
		for (Entry<String, Collection<String>> e : dirFiles.entrySet()) {
			for (String f : e.getValue()) {
				tree.addItem(f);
				tree.setItemCaption(f, shortName(f));
				tree.setChildrenAllowed(f, false);
			}
			String dir = e.getKey();
			if (!"".equals(dir)) {
				tree.addItem(dir);
				for (String f : e.getValue()) {
					tree.setParent(f, dir);
				}
			}
		}
	}

	private String shortName(String f) {
		return f.substring(f.lastIndexOf('/')+1); // works for -1 too
	}

	private static Map<String, Collection<String>> getFilesByDir(Collection<String> names) {
		TreeMap<String, Collection<String>> files = new TreeMap<String, Collection<String>>();
		for (String n : names) {
			int i = n.lastIndexOf('/');
			String dir;
			if (i == -1) {
				dir = "";
			}
			else {
				dir = n.substring(0, i);
			}
			Collection<String> fs = files.get(dir);
			if (fs == null) {
				fs = new TreeSet<String>();
				files.put(dir, fs);
			}
			fs.add(n);
		}
		return files;
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
			fireFileSelected(file);
		}
	}

	@Override
	public void itemClick(ItemClickEvent event) {
		if (event.isCtrlKey() || event.isMetaKey() || event.isDoubleClick()) {
			fireFileSelectedNewTab((String) event.getItemId());
		}

	}

	private void fireFileSelected(String name) {
		for (Listener li : listeners) {
			li.selected(name);
		}
	}

	private void fireFileSelectedNewTab(String name) {
		for (Listener li : listeners) {
			li.selectedNewTab(name);
		}
	}

}
