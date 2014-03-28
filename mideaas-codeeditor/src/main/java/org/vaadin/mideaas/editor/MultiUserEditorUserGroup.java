package org.vaadin.mideaas.editor;

import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.Map;

import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;
import org.vaadin.mideaas.editor.EditorState.DocType;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.util.ReflectTools;


@SuppressWarnings("serial")
public class MultiUserEditorUserGroup extends CustomComponent
		implements ValueChangeListener, DifferingChangedListener {
	
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

	private final EditorUser user;
	private final MultiUserDoc mud;
	private final EditorState mineThatEqualsBase;
	
	
	private EditorState currentState;

	
	public MultiUserEditorUserGroup(EditorUser user, MultiUserDoc mud) {
		this.user = user;
		this.mud = mud;
		mineThatEqualsBase = new EditorState(DocType.MINE, new DocDifference(user, null, null));
		currentState = mineThatEqualsBase;
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
		setDifferences(mud.getDifferences());
		mud.addDifferingChangedListener(this);
		group.addValueChangeListener(this);
	}
	
	@Override
	public void detach() {
		
		mud.removeDifferingChangedListener(this);
		
		super.detach();
	}

	private void setDifferences(Map<EditorUser, DocDifference> map) {
		group.removeAllItems();
		
		if (map.isEmpty()) {
			group.addItem(mineThatEqualsBase);
			group.select(mineThatEqualsBase);
			setCurrentState(mineThatEqualsBase);
			return;
		}

		DocDifference my = map.get(user);
		EditorState myState;
		if (my==null) {
			myState = mineThatEqualsBase;
		}
		else {
			group.addItem(EditorState.BASE_STATE);
			myState = new EditorState(DocType.MINE, my);
		}
		group.addItem(myState);
		
		for (DocDifference d : map.values()) {
			if (!d.getUser().equals(user)) {
				group.addItem(new EditorState(DocType.OTHERS, d));
			}
		}
		
		if (group.containsId(currentState)) {
			group.select(currentState);
		}
		else {
			group.select(myState);
			setCurrentState(myState);
		}
		
		
	}

	private void setCurrentState(EditorState state) {
		if (state.equals(currentState)) {
			return;
		}
		
		currentState = state;
		fireEvent(new EditorStateChangedEvent(this, currentState));
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		EditorState st = (EditorState)event.getProperty().getValue();
		if (st!=null) {
			setCurrentState(st);
		}
		
	}

	@Override
	public void differingChanged(final Map<EditorUser, DocDifference> diffs) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				setDifferences(diffs);
			}
		});
	}
	
	
}
