package org.vaadin.mideaas.ide;

import java.util.List;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.TeamLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class Ide extends CustomComponent {

	private final IdeProject project;
	private final IdeUser user;
	private final EditorUser editorUser;
	private final IdeConfiguration config;

	private final HorizontalSplitPanel split = new HorizontalSplitPanel();
	private IdeDoc activeDoc;
	private MenuBar menuBar;
	private VerticalLayout sidebarLayout;
	private final IdeEditorComponent editorComponent;

	public Ide(IdeProject project, IdeUser user, IdeConfiguration config) {

		this.project = project;
		this.user = user;
		this.editorUser = user.getEditorUser();
		this.config = config;
		this.editorComponent = new IdeEditorComponent();

		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		la.addComponent(createMenuBar());
		
		split.setSizeFull();
		split.setFirstComponent(createSidebar());
		split.setSecondComponent(editorComponent);
		split.setSplitPosition(200, Unit.PIXELS);
		la.addComponent(split);
		
		la.setExpandRatio(split, 1);

		setCompositionRoot(la);
		
		closeDoc();
	}
	
	public IdeProject getProject() {
		return project;
	}
	
	public IdeUser getUser() {
		return user;
	}
	
	public MenuBar getMenuBar() {
		return menuBar;
	}
	
	public void addSideBarComponents(List<Component> components) {
		for (Component c : components) {
			sidebarLayout.addComponent(c);
		}
	}
	
	@Override
	public void attach() {
		super.attach();
		project.getTeam().addUser(editorUser);
	}
	
	@Override
	public void detach() {
		super.detach();
		project.getTeam().removeUser(editorUser);
	}
	
	public void closeDoc() {
		setActiveDoc(null);
		editorComponent.removeEditor();
	}

	public void openDoc(String name) {
		IdeDoc doc = project.getDoc(name);
		if (doc == null) {
			return;
		}
		
		setActiveDoc(doc);
		
		// XXX
		Suggester suggester = project.createSuggesterFor(name, user);
		
		editorComponent.setEditor(doc, user, suggester);
	}
	
	private void setActiveDoc(IdeDoc doc) {
		if (activeDoc != null) {
			// TODO??? activeDoc.getDoc().removeChildDoc(user);
		}
		activeDoc = doc;
	}
	
	private MenuBar createMenuBar() {
		menuBar = new MenuBar();
		menuBar.setWidth("100%");
		MenuItem userMenu = menuBar.addItem(editorUser.getName(), null, null);
		userMenu.addItem("Log out", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				((IdeUI)getUI()).logOut();
			}
		});
		
		return menuBar;
	}

	private Component createSidebar() {
		
		VerticalSplitPanel split = new VerticalSplitPanel();
		split.setSizeFull();
		split.setSplitPosition(250, Unit.PIXELS);
		
		FileList fileList = new FileList(project, config);
		fileList.setSizeFull();
		fileList.addListener(new FileList.Listener() {
			@Override
			public void selected(String name) {
				openDoc(name);
			}
			@Override
			public void selectedNewTab(String name) {
				getUI().getPage().open("#!"+project.getId()+"/"+name, "_blank");
			}
		});

		split.setFirstComponent(fileList);
		
		sidebarLayout = new VerticalLayout();
		sidebarLayout.setSpacing(true);

		TeamLayout tela = new TeamLayout(project.getTeam());
		tela.setMaxCols(4);
		sidebarLayout.addComponent(tela);

		IdeChatBox chat = new IdeChatBox(project.getChat(), editorUser);
		chat.setWidth("100%");
		sidebarLayout.addComponent(chat);

		Panel pa = new Panel(sidebarLayout);
		pa.setSizeFull();
		split.setSecondComponent(pa);

		return split;
	}

	public void setBelowEditorComponent(Component component, int initialHeightPixels) {
		editorComponent.setBelowEditorComponent(component, initialHeightPixels);
	}

	public void setBelowEditorComponent(Component component) {
		editorComponent.setBelowEditorComponent(component);
	}	

}
