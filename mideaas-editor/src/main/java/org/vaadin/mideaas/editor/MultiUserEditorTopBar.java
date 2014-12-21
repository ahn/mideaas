package org.vaadin.mideaas.editor;

import java.util.Map;

import org.vaadin.mideaas.editor.user.UserSquare;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class MultiUserEditorTopBar extends CustomComponent {

	private final MultiUserEditor parent;
	private final HorizontalLayout layout;
	private Label titleLabel = new Label();

	public MultiUserEditorTopBar(MultiUserEditor parent, EditorUser user) {
		this.parent = parent;
		//setWidth("100%");
		layout = new HorizontalLayout();
		//layout.setWidth("100%");
		layout.setSpacing(false);
		this.addStyleName("collabeditor-layout");
		if (user != null) {
			this.addStyleName("collabeditor-layout-"+user.getStyleIndex());
		}
		titleLabel.addStyleName("collabeditor-title");
		//		titleLabel.setWidth("150px"); // ???
		setCompositionRoot(layout);
	}

	public void setTitle(String title) {
		titleLabel.setValue(title);
	}

	public void setDiffering(Map<EditorUser, DocDifference> diffs) {
		layout.removeAllComponents();
		layout.addComponent(titleLabel);
		layout.setComponentAlignment(titleLabel, Alignment.BOTTOM_LEFT);
		//layout.setExpandRatio(titleLabel, 1);

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
