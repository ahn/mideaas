package org.vaadin.mideaas.ide;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.vaadin.mideaas.ide.PomXml.Dependency;
import org.vaadin.mideaas.ide.model.SharedProject;
import org.xml.sax.SAXException;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AddonManagementWindow extends Window {

	private final SharedProject project;

	private Table table = new Table("Dependencies");
	private TextArea addon = new TextArea("Dependency Maven XML snippet");
	private Button removeButton = new Button("Remove selected");
	private Button addButton = new Button("Add Dependency");

	public AddonManagementWindow(SharedProject project) {
		super("Add-ons");
		this.project = project;
		table.setSizeUndefined();
		table.setSelectable(true);
		table.setPageLength(0);
		table.addContainerProperty("groupId", String.class, "foo");
		table.addContainerProperty("artifactId", String.class, "bar");
		table.addContainerProperty("version", String.class, "baz");
	}

	@Override
	public void attach() {
		super.attach();

		VerticalLayout la = new VerticalLayout();
		la.setMargin(true);
		setContent(la);

		la.addComponent(table);
		la.addComponent(removeButton);
		la.addComponent(addon);
		la.addComponent(addButton);
		
		removeButton.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				Item d = table.getItem(table.getValue());
				String groupId = (String)d.getItemProperty("groupId").getValue();
				String artifactId = (String)d.getItemProperty("artifactId").getValue();
				String version = (String)d.getItemProperty("version").getValue();
				removeAddon(groupId, artifactId, version);
			}
		});

		addButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String s = addon.getValue();
				if (s!=null && !s.isEmpty()) {
					addAddon(s);
				}
			}
		});
		
		updateTable();
	}

	private void removeAddon(String groupId, String artifactId, String version) {
		project.removeDependency(new Dependency(groupId, artifactId, version));
		updateTable();
	}

	private void addAddon(String s) {
		try {
			project.addDependency(s);
			updateTable();
		} catch (ParserConfigurationException | SAXException | IOException | TransformerFactoryConfigurationError | TransformerException e) {
			Notification.show("Could not add dependency: "+e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
			
	}

	private void updateTable() {
		table.removeAllItems();
		for (Dependency d : project.getDependencies()) {
			table.addItem(new Object[] {d.groupId, d.artifactId, d.version}, d);
		}
	}

}
