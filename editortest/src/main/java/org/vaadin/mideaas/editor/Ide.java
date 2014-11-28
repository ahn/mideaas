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

	public Ide(MultiUserProject project, EditorUser user) {

		this.project = project;
		this.user = user;

		split.setSizeFull();
		split.setFirstComponent(createFirstComponent());
		split.setSplitPosition(200, Unit.PIXELS);

		setCompositionRoot(split);
	}

	public void openDoc(String name) {

		IdeDoc doc = project.getDoc(name);
		if (doc == null) {
			return;
		}
		MultiUserEditor ed = new MultiUserEditor(user, doc.getDoc(),
				doc.getAceMode());
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

	private Component createFirstComponent() {
		FileList fileList = new FileList(project);
		fileList.addListener(new FileList.Listener() {
			@Override
			public void selected(String name) {
				openDoc(name);
			}
		});

		VerticalLayout la = new VerticalLayout();
		la.setSpacing(true);

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
