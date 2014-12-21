package org.vaadin.mideaas.editor.user.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class UserSquareWidget extends FocusPanel {

	private final AbsolutePanel panel = new AbsolutePanel();
	private final Label nameLabel = new Label();
	private final Image image = new Image();
	private final Label errorLabel = new Label();

	public UserSquareWidget() {
		create();
	}

	private void create() {
		setStyleName("user-square");
		add(panel);
		panel.add(image, 4, 4);
		panel.add(nameLabel, 4, 0);
		panel.add(errorLabel, 1, 1);
		nameLabel.addStyleName("name");
		nameLabel.setWordWrap(false);
		errorLabel.addStyleName("error");
		setErrorSize(0);
	}

	public void setName(String name) {
		nameLabel.setText(name);
		image.setAltText(name);
	}

	public void setImageUrl(String url) {
		image.setUrl(url);
	}

	public void setSize(int size) {
		panel.setPixelSize(size, size);
		int imageSize = size - 8;
		image.setPixelSize(imageSize, imageSize);
		panel.setWidgetPosition(nameLabel, 4, size - 4 - 12);
		nameLabel.setPixelSize(imageSize, 12);
	}

	public void setErrorSize(int errorSize) {
		if (errorSize == 0) {
			errorLabel.setVisible(false);
		}
		else {
			errorLabel.setVisible(true);
			int es = errorSize < 6 ? 6 : errorSize;
			errorLabel.setPixelSize(es, es);
		}
	}
}