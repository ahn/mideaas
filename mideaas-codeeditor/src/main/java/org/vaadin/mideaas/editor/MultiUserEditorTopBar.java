package org.vaadin.mideaas.editor;

import java.util.Map;

import org.vaadin.mideaas.editor.DocDifference;
import org.vaadin.mideaas.editor.EditorUser;

import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class MultiUserEditorTopBar extends CustomComponent {
	
	private final MultiUserEditor parent;
	private final EditorUser user;
	private final HorizontalLayout layout;
	
	public MultiUserEditorTopBar(MultiUserEditor parent, EditorUser user) {
		this.parent = parent;
		this.user = user;
		layout = new HorizontalLayout();
		layout.setSpacing(false);
		if (user != null) {
			this.addStyleName("collab-user-"+user.getStyleIndex());
		}
		setCompositionRoot(layout);
	}

	public void setDiffering(Map<EditorUser, DocDifference> diffs) {
		layout.removeAllComponents();
		
		NativeButton base = new NativeButton("Base");
		base.addStyleName("collab-button");
		base.setEnabled(user!=null);
		base.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				parent.userClicked(null);
			}
		});
		layout.addComponent(base);
		
		for (final DocDifference dd : diffs.values()) {
			NativeButton b = new NativeButton(dd.buttonText());
			b.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					parent.userClicked(dd.getUser());
				}
			});
			layout.addComponent(b);
			b.addStyleName("collab-button");
			if (dd.isChanged()) {
				b.addStyleName("different");
			}
			if (dd.getUser().equals(user)) {
				b.addStyleName("collab-user-me");
				b.setEnabled(false);
			}
			b.addStyleName("collab-user-" + dd.getUser().getStyleIndex());
		}
	}
	

}
