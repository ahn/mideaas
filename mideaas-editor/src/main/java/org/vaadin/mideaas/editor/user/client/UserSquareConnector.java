package org.vaadin.mideaas.editor.user.client;

import org.vaadin.mideaas.editor.user.UserSquare;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(UserSquare.class)
public class UserSquareConnector extends AbstractComponentConnector {

	UserSquareServerRpc rpc = RpcProxy.create(UserSquareServerRpc.class, this);

	public UserSquareConnector() {
		getWidget().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final MouseEventDetails mouseDetails = MouseEventDetailsBuilder
						.buildMouseEventDetails(event.getNativeEvent(),
								getWidget().getElement());

				// When the widget is clicked, the event is sent to server with ServerRpc
				rpc.clicked(mouseDetails);
			}
		});

	}

	// We must implement createWidget() to create correct type of widget
	@Override
	protected Widget createWidget() {
		return GWT.create(UserSquareWidget.class);
	}


	// We must implement getWidget() to cast to correct type
	@Override
	public UserSquareWidget getWidget() {
		return (UserSquareWidget) super.getWidget();
	}

	// We must implement getState() to cast to correct type
	@Override
	public UserSquareState getState() {
		return (UserSquareState) super.getState();
	}

	// Whenever the state changes in the server-side, this method is called
	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		UserSquareWidget w = getWidget();
		w.setSize(getState().size);
		w.setName(getState().name);
		if (getState().style != null) {
			w.addStyleName(getState().style);
		}
		if (getState().imageUrl != null) {
			w.setImageUrl(getState().imageUrl);
		}
		w.setErrorSize(getState().errorSize);
	}

}
