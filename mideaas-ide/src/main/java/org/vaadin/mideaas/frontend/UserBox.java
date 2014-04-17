package org.vaadin.mideaas.frontend;

import org.vaadin.mideaas.model.User;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class UserBox extends Panel {
	
	private final User user;
	private VerticalLayout layout = new VerticalLayout();
	//private Label label = null;
	
	public UserBox(User user) {
		
		this.user = user;
		setSizeUndefined();
		
		layout.setSizeUndefined();
		//layout.setMargin(true);
		layout.setStyleName("userbox-layout");
		
		Label label = new Label(user.getName());
		label.setSizeUndefined();
		layout.addComponent(label);
		layout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
		//label.addStyleName("user-label");
		
		//setSizeUndefined();
		//label.setSizeUndefined();
		//setWidth("40px");
		//setHeight("20px");
		//this.user = user;
		//layout.setSizeFull();
		setContent(layout);
		//setContent(label);
	}
	
	@Override
	public void attach() {
		super.attach();
//		String name = String.format("<strong>%s</strong>", user.getName());
//		layout.addComponent(new Label(name,ContentMode.HTML));
		/*
		String imgUrl = user.getImgUrl();
		if (imgUrl!=null) {
			Image img = new Image(null, new ExternalResource(imgUrl));
			img.setWidth("50px");
			img.setHeight("50px");
			layout.addComponent(img);
			layout.setComponentAlignment(img, Alignment.MIDDLE_CENTER);
		}
		*/
		//setCaption(user.getName());
		//setDescription(user.getName());
		//label.setCaption(user.getName());
	}

}
