package org.vaadin.mideaas.ide;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class FileList extends CustomComponent implements
		IdeProjectWithWorkDir.Listener, ValueChangeListener, ItemClickListener, Handler {

	public interface Listener {
		public void selected(String name);
		public void selectedNewTab(String name);
	}

	private LinkedList<Listener> listeners = new LinkedList<Listener>();

	private final IdeProject project;
	private final IdeConfiguration config;
	private final Tree tree;

	private final Action ACTION_DELETE = new ShortcutAction("Delete file", ShortcutAction.KeyCode.DELETE, null);
	private final Action ACTION_INSERT = new ShortcutAction("Add new...");
	private final Action ACTION_OPEN_RAW = new ShortcutAction("View raw");
	private final Action ACTION_OPEN_TAB = new ShortcutAction("Open in new tab", ShortcutAction.KeyCode.ENTER, null);

	public FileList(IdeProject project, IdeConfiguration config) {
		this.project = project;
		this.config = config;
		tree = new Tree();
		tree.setSizeFull();
		tree.setSelectable(true);
		tree.addItemClickListener(this);
		tree.addActionHandler(this);
		tree.addValueChangeListener(this);

		VerticalLayout la = new VerticalLayout();
		la.addComponent(tree);
		la.setSizeFull();
		
		Panel pa = new Panel(project.getName());
		pa.setContent(la);
		pa.setSizeFull();
		pa.addActionHandler(this);

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

		updateTable(project.getDocIds());

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

	@Override
	public Action[] getActions(Object target, Object sender) {
		String filename = (String) target;
		if (filename != null && project.getDoc(filename) != null) {
			return new Action[] { ACTION_INSERT, ACTION_DELETE, ACTION_OPEN_RAW, ACTION_OPEN_TAB };
		}
		else {
			return new Action[] { ACTION_INSERT };
		}
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == ACTION_DELETE) {
			final String filename = (String) target;
			if (filename == null || project.getDoc(filename) == null) {
				return;
			}
			final String msg = "Delete "+filename+"?";
			ConfirmDialog.show(getUI(), msg, msg, "Yes", "No",  new ConfirmDialog.Listener() {
				@Override
				public void onClose(ConfirmDialog d) {
					if (d.isConfirmed()) {
						project.removeDoc(filename);
					}
				}
			});
		}
		else if (action == ACTION_INSERT) {
			final String filename = (String) target;
			final Window win = new Window("Add new...");
			DocAdderComponent dac = config.createDocAdderComponent(project, filename, new DocAdderImpl(project) {
				@Override
				public void done() {
					win.close();
				}
			});
			win.setContent(dac);
			win.center();
			getUI().addWindow(win);
		}
		else if (action == ACTION_OPEN_RAW) {
			final String filename = (String) target;
			if (filename == null || project.getDoc(filename) == null) {
				return;
			}
			String root = ((IdeUI)UI.getCurrent()).getServerRootUrl();
			getUI().getPage().open(root + "raw/"+project.getId()+"/"+filename, "_blank");
		}
		else if (action == ACTION_OPEN_TAB) {
			final String filename = (String) target;
			if (filename == null || project.getDoc(filename) == null) {
				return;
			}
			getUI().getPage().open("#!"+project.getId()+"/"+filename, "_blank");
		}
		
	}

}
