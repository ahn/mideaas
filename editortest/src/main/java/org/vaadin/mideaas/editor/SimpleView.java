package org.vaadin.mideaas.editor;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class SimpleView extends CustomComponent implements View {

	private final String title;
	
	public SimpleView(IdeDoc doc, EditorUser user, String title) {
		this.title = title;
		
		MultiUserEditor ed = new MultiUserEditor(user, doc.getDoc(), doc.getAceMode());
		ed.setSizeFull();
		
		IdeChatBox chat = new IdeChatBox(doc.getChat(), user);
		chat.setSizeFull();
		
		VerticalSplitPanel split = new VerticalSplitPanel();
		split.setSizeFull();
		this.setSizeFull();
		split.setFirstComponent(ed);
		split.setSecondComponent(chat);
		split.setSplitPosition(200, Unit.PIXELS, true);
		setCompositionRoot(split);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		getUI().getPage().setTitle(title);
	}

}
