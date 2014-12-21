package org.vaadin.mideaas.editor;

import java.util.Map;

import org.vaadin.mideaas.editor.user.UserSquare;

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

		UserSquare base = new UserSquare("Base", 32);
		base.addClickListener(new ClickListener() {
			@Override
			public void click(ClickEvent event) {
				parent.userClicked(null);
			}
		});
		layout.addComponent(base);

		for (final DocDifference dd : diffs.values()) {
			UserSquare b = new UserSquare(dd.getUser(), 32);
			if (dd.isChanged()) {
				b.setErrorAmount(errorAmount(dd));
			}
			b.addClickListener(new ClickListener() {
				@Override
				public void click(ClickEvent event) {
					parent.userClicked(dd.getUser());
				}
			});
			layout.addComponent(b);
		}
	}

	private static double errorAmount(DocDifference dd) {
		int err = dd.getInserts() + dd.getDeletes();
		return Math.min(1.0, Math.sqrt(err*0.0025));
	}


}
