package org.vaadin.mideaas.editor;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class Ide extends CustomComponent {

	private final MultiUserProject project;
	private final EditorUser user;

	private final HorizontalSplitPanel split = new HorizontalSplitPanel();
	private IdeDoc activeDoc;

	public Ide(MultiUserProject project, IdeUser user) {

		this.project = project;
		this.user = user.getEditorUser();

		split.setSizeFull();
		split.setFirstComponent(createSidebar());
		split.setSplitPosition(200, Unit.PIXELS);
		

		setCompositionRoot(split);
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
		
		MultiUserEditor ed = new MultiUserEditor(user, doc.getDoc(), doc.getAceMode());
		ed.setSizeFull();

		VerticalSplitPanel vsplit = new VerticalSplitPanel();
		vsplit.setSizeFull();
		vsplit.setFirstComponent(ed);

		IdeChatBox chat = new IdeChatBox(doc.getChat(), user);
		chat.setSizeFull();

		vsplit.setSecondComponent(chat);

		vsplit.setSplitPosition(200, Unit.PIXELS, true);

		split.setSecondComponent(vsplit);

	}
	
	private void setActiveDoc(IdeDoc doc) {
		if (activeDoc != null) {
			// TODO activeDoc.getDoc().removeChildDoc(user);
		}
		activeDoc = doc;
	}

	private Component createSidebar() {
		VerticalLayout la = new VerticalLayout();
		la.setSpacing(true);
		
		FileList fileList = new FileList(project);
		fileList.addListener(new FileList.Listener() {
			@Override
			public void selected(String name) {
				openDoc(name);
			}
			@Override
			public void selectedNewTab(String name) {
				getUI().getPage().open("#!"+project.getName()+"/"+name, "_blank");
			}
		});

		la.addComponent(fileList);

		TeamLayout cp = new TeamLayout(project.getTeam());
		cp.setMaxCols(1);
		la.addComponent(cp);

		IdeChatBox chat = new IdeChatBox(project.getChat(), user);
		chat.setWidth("100%");
		la.addComponent(chat);

		return la;
	}

}
