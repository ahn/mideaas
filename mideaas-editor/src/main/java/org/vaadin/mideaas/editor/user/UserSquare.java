package org.vaadin.mideaas.editor.user;

import java.util.LinkedList;

import org.vaadin.mideaas.editor.EditorUser;
import org.vaadin.mideaas.editor.user.client.UserSquareServerRpc;
import org.vaadin.mideaas.editor.user.client.UserSquareState;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.shared.MouseEventDetails;

@SuppressWarnings("serial")
public class UserSquare extends com.vaadin.ui.AbstractComponent {

	private UserSquareServerRpc rpc = new UserSquareServerRpc() {

		@Override
		public void clicked(MouseEventDetails mouseDetails) {
			fireClicked(mouseDetails);
		}
	};

	private final EditorUser user;
	private int size;
	private double errorAmount = 0.0;

	public UserSquare(String text, int size) {
		user = null;
		this.size = size;
		getState().name = text;
		registerRpc(rpc);
		setSize(size);
	}

	public UserSquare(EditorUser user, int size) {
		this.user = user;
		this.size = size;
		getState().name = user.getName();
		getState().style = "user-" + user.getStyleIndex();
		registerRpc(rpc);
		setSize(size);
	}

	public void setSize(int size) {
		assert size >= 16;
		setWidth(size+"px");
		setHeight(size+"px");
		getState().size = size;
		if (user != null) {
			getState().imageUrl = user.getGravatarUrl(size-8);
		}
		setErrorAmount(errorAmount);
	}

	@Override
	public UserSquareState getState() {
		return (UserSquareState) super.getState();
	}

	private final LinkedList<ClickListener> listeners = new LinkedList<ClickListener>();

	public void addClickListener(ClickListener listener) {
		listeners.add(listener);
	}

	public void removeClickListener(ClickListener listener) {
		listeners.remove(listener);
	}

	public void setErrorAmount(double errorAmount) {
		assert errorAmount >= 0.0 && errorAmount <= 1.0;
		getState().errorSize = (int) (maxErrorSize() * errorAmount);
	}

	private int maxErrorSize() {
		return size - 2;
	}

	private void fireClicked(MouseEventDetails mouseDetails) {
		for (ClickListener li : listeners) {
			li.click(new ClickEvent(this, mouseDetails));
		}
	}
}
