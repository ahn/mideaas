package org.vaadin.mideaas.editor;

import java.util.Map;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class MultiUserEditorTopBar extends CustomComponent {
	
	private final MultiUserEditor parent;
	private final HorizontalLayout layout;
	
	public MultiUserEditorTopBar(MultiUserEditor parent, EditorUser user) {
		this.parent = parent;
		layout = new HorizontalLayout();
		layout.setSpacing(false);
		this.addStyleName("collabeditor-layout");
		if (user != null) {
			this.addStyleName("collabeditor-layout-"+user.getStyleIndex());
		}
		setCompositionRoot(layout);
	}

	public void setDiffering(Map<EditorUser, DocDifference> diffs) {
		layout.removeAllComponents();
		
		UserSquare base = new UserSquare(null, 16);
		base.addClickListener(new ClickListener() {
			@Override
			public void click(ClickEvent event) {
				parent.userClicked(null);
			}
		});
		layout.addComponent(base);
		
		for (final DocDifference dd : diffs.values()) {
			UserSquare b = new UserSquare(dd.getUser(), 16, dd.isChanged());
			b.addClickListener(new ClickListener() {
				@Override
				public void click(ClickEvent event) {
					parent.userClicked(dd.getUser());
				}
			});
			layout.addComponent(b);
		}
	}
	

}
