package org.vaadin.mideaas.editor;

import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.Set;

import org.vaadin.mideaas.editor.EditorState.DocType;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingUsersChangedListener;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.util.ReflectTools;


@SuppressWarnings("serial")
public class MultiUserEditorUserGroup extends CustomComponent
		implements DifferingUsersChangedListener, ValueChangeListener {
	
	public interface EditorStateChangedListener {
		public void stateChanged(EditorStateChangedEvent event);
		public static final Method METHOD = ReflectTools
                .findMethod(EditorStateChangedListener.class, "stateChanged",
                		EditorStateChangedEvent.class);
	}

	public static class EditorStateChangedEvent extends EventObject {
		public final EditorState state;
		private EditorStateChangedEvent(MultiUserEditorUserGroup source, EditorState state) {
			super(source);
			this.state = state;
		}
	}

	private final HorizontalOptionGroup group = new HorizontalOptionGroup();

	private final String userId;
	private final MultiUserDoc doc;
	private final EditorState mine;
	
	
	private EditorState currentState;

	
	public MultiUserEditorUserGroup(String userId, MultiUserDoc doc) {
		this.userId = userId;
		this.doc = doc;
		mine = new EditorState(DocType.MINE, userId);
		currentState = mine;
		group.setImmediate(true);
		group.setNullSelectionAllowed(false);
		setCompositionRoot(group);
	}
	
	public EditorState getEditorState() {
		return currentState;
	}

	public void addDocStateChangedListener(EditorStateChangedListener li) {
		addListener(EditorStateChangedEvent.class, li,
				EditorStateChangedListener.METHOD);
	}
	
	public void removeDocStateChangedListener(EditorStateChangedListener li) {
		removeListener(EditorStateChangedEvent.class, li);
	}
	
	@Override
	public void attach() {
		super.attach();
		setDifferingUsers(doc.getDifferingUsers());
		doc.addDifferingUsersChangedListener(this);
		group.addValueChangeListener(this);
	}
	
	@Override
	public void detach() {
		super.detach();
		
		doc.removeDifferingUsersChangedListener(this);
	}
	
	@Override
	public void differingUsersChanged(final Set<String> users) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				setDifferingUsers(users);
			}
		});
	}

	private void setDifferingUsers(Set<String> users) {
		group.removeAllItems();
		
		if (users.isEmpty()) {
			group.addItem(mine);
			group.select(mine);
			setCurrentState(mine);
			return;
		}
		
		EditorState base = new EditorState(DocType.BASE, null);
		
		group.addItem(mine);
		
		if (users.contains(userId)) {
			group.addItem(base);
		}
		
		for (String uid : users) {
			if (!uid.equals(userId)) {
				group.addItem(new EditorState(DocType.OTHERS, uid));
			}
		}
		
		if (group.containsId(currentState)) {
			group.select(currentState);
		}
		else {
			group.select(mine); //
			setCurrentState(mine);
		}
		
	}
	
	private void setCurrentState(EditorState state) {
		if (state.equals(currentState)) {
			return;
		}
		
		currentState = state;
		fireChange(currentState);
	}

	private void fireChange(EditorState state) {
		fireEvent(new EditorStateChangedEvent(this, state));
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		EditorState st = (EditorState)event.getProperty().getValue();
		if (st==null) {
			st = mine;
		}
		setCurrentState(st);
	}
}
