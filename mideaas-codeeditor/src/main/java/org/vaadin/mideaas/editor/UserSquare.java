package org.vaadin.mideaas.editor;

import com.vaadin.event.MouseEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;

@SuppressWarnings("serial")
public class UserSquare extends CustomComponent {

	private final EditorUser user;
	private final int imageSize;
	private final boolean active;
	private final Image image;

	public UserSquare(EditorUser user, int imageSize) {
		this(user, imageSize, true);
	}
	
	public UserSquare(EditorUser user, int imageSize, boolean active) {
		this.user = user;
		this.imageSize = imageSize;
		this.active = active;
		image = draw();
	}
	
	private Image draw() {
		AbsoluteLayout  la = new AbsoluteLayout();
		la.setWidth((imageSize+8)+"px");
		la.setHeight((imageSize+8)+"px");
		Image img;
		if (user!=null) {
			img = new Image(null, new ExternalResource(user.getGravatarUrl(imageSize)));
			la.addStyleName("collab-user-" + user.getStyleIndex());
			la.setDescription(user.getName());
		}
		else {
			img = new Image();
		}
		
		img.setWidth(imageSize+"px");
		img.setHeight(imageSize+"px");
		la.addStyleName("collab-square");
		la.addComponent(img, "left: 4px; top: 4px");
		
		img.addStyleName("collab-square-image");
		if (active) {
			img.addStyleName("collab-square-image-active");
		}
		else {
			img.addStyleName("collab-square-image-passive");
		}

		/*
		if (text!=null) {
			Label label = new Label(text);
			label.addStyleName("collab-square-label");
			if (user != null) {
				label.addStyleName("collab-user-"+user.getStyleIndex());
			}
			label.setWidth(imageSize+"px");
			label.setHeight(14+"px");
			la.addComponent(label, "left: 2px; top: 0px");
		}
		*/
		
		setCompositionRoot(la);
		return img;
	}

	
	public void addClickListener(MouseEvents.ClickListener listener) {
		image.addClickListener(listener);
	}
	
	public void removeClickListener(MouseEvents.ClickListener listener) {
		image.removeClickListener(listener);
	}
	
}
