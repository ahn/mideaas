package org.vaadin.mideaas.ide;

import java.util.List;

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
	private final IdeUser ideUser;
	private final EditorUser user;
	private final IdeCustomizer customizer;

	private final HorizontalSplitPanel split = new HorizontalSplitPanel();
	private IdeDoc activeDoc;

	public Ide(IdeProject project, IdeUser user, IdeCustomizer customizer) {

		this.project = project;
		this.ideUser = user;
		this.user = user.getEditorUser();
		this.customizer = customizer;

		VerticalLayout la = new VerticalLayout();
		la.setSizeFull();
		la.addComponent(createMenuBar());
		
		split.setSizeFull();
		split.setFirstComponent(createSidebar());
		split.setSplitPosition(200, Unit.PIXELS);
		la.addComponent(split);
		
		la.setExpandRatio(split, 1);

		setCompositionRoot(la);
	}
	
	

	@Override
	public void attach() {
		super.attach();
		project.getTeam().addUser(user);
	}
	
	@Override
	public void detach() {
		super.detach();
		project.getTeam().removeUser(user);
	}

	public void openDoc(String name) {
		
		IdeDoc doc = project.getDoc(name);
		if (doc == null) {
			return;
		}
		
		setActiveDoc(doc);
		
		IdeEditorComponent ed = new IdeEditorComponent(customizer, project, doc, ideUser);

		split.setSecondComponent(ed);

	}
	
	private void setActiveDoc(IdeDoc doc) {
		if (activeDoc != null) {
			// TODO activeDoc.getDoc().removeChildDoc(user);
		}
		activeDoc = doc;
	}
	
	private MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();
		menuBar.setWidth("100%");
		MenuItem userMenu = menuBar.addItem(user.getName(), null, null);
		userMenu.addItem("Log out", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				((IdeUI)getUI()).logOut();
			}
		});
		
		customizer.customizeMenuBar(menuBar);
		
		return menuBar;
	}

	private Component createSidebar() {
		
		VerticalSplitPanel split = new VerticalSplitPanel();
		split.setSizeFull();
		split.setSplitPosition(250, Unit.PIXELS);
		
		FileList fileList = new FileList(project);
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
		
		VerticalLayout la = new VerticalLayout();
		la.setSpacing(true);

		TeamLayout tela = new TeamLayout(project.getTeam());
		tela.setMaxCols(4);
		la.addComponent(tela);

		IdeChatBox chat = new IdeChatBox(project.getChat(), user);
		chat.setWidth("100%");
		la.addComponent(chat);
		
		List<Component> additional = customizer.getSidebarComponents(project, ideUser);
		if (additional != null) {
			for (Component c : additional) {
				la.addComponent(c);
			}
		}

		Panel pa = new Panel(la);
		pa.setSizeFull();
		split.setSecondComponent(pa);

		return split;
	}

}
