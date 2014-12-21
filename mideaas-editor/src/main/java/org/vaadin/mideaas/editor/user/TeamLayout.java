package org.vaadin.mideaas.editor.user;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.mideaas.editor.EditorUser;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@StyleSheet("team-layout.css")
public class TeamLayout extends CustomComponent implements Team.Listener {

	public interface UserClickListener {
		public void clicked(EditorUser user);
	}
	
	private final Team team;
	private int squareSize;
	private int maxCols;
	private final LinkedList<UserClickListener> listeners = new LinkedList<UserClickListener>();

	public TeamLayout(Team team) {
		this.team = team;
		this.squareSize = 48;
		this.maxCols = 0;
		this.setPrimaryStyleName("team-layout"); // ???
	}
	
	public void setImageSize(int imageSize) {
		if (this.squareSize == imageSize) {
			return;
		}
		this.squareSize = imageSize;
		if (isAttached()) {
			update(team.getUsers());
		}
	}
	
	public void setMaxCols(int maxCols) {
		if (this.maxCols == maxCols) {
			return;
		}
		this.maxCols = maxCols;
		if (isAttached()) {
			update(team.getUsers());
		}
	}

	@Override
	public void attach() {
		super.attach();
		team.addListener(this);
		update(team.getUsers());
	}
	
	@Override
	public void detach() {
		team.removeListener(this);
		super.detach();
	}

	@Override
	public void changed(final List<EditorUser> users) {
		UI ui = getUI();
		if (ui == null) {
			return;
		}
		ui.access(new Runnable() {
			@Override
			public void run() {
				update(users);
			}
		});
	}
	
	public void addUserClickListener(UserClickListener li) {
		listeners.add(li);
	}
	
	public void removeUserClickListener(UserClickListener li) {
		listeners.remove(li);
	}
	
	private void update(List<EditorUser> users) {
		if (users.isEmpty()) {
			setCompositionRoot(new Label());
			return;
		}

		Layout la;
		if (maxCols > 0) {
			int h = (users.size()-1) / maxCols + 1;
			la = new GridLayout(maxCols,h);
		}
		else {
			la = new HorizontalLayout();
		}
		for (final EditorUser user : users) {
			UserSquare square = new UserSquare(user, squareSize);
			square.addClickListener(new ClickListener() {
				@Override
				public void click(ClickEvent event) {
					fireUserClicked(user);
				}
			});
			la.addComponent(square);
		}
		setCompositionRoot(la);
	}

	private void fireUserClicked(EditorUser user) {
		for (UserClickListener li : listeners) {
			li.clicked(user);
		}
	}
}