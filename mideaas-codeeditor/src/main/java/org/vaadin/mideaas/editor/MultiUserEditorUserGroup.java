package org.vaadin.mideaas.editor;

import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.Set;

import org.vaadin.mideaas.editor.EditorState.DocType;
import org.vaadin.mideaas.editor.MultiUserDoc.DifferingChangedListener;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.util.ReflectTools;


@SuppressWarnings("serial")
public class MultiUserEditorUserGroup extends CustomComponent
		implements DifferingChangedListener, ValueChangeListener {
	
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
	private final EditorState mineThatEqualsBase;
	
	
	private EditorState currentState;

	
	public MultiUserEditorUserGroup(String userId, MultiUserDoc doc) {
		this.userId = userId;
		this.doc = doc;
		mineThatEqualsBase = new EditorState(DocType.MINE, new DocDifference(userId, null, null));
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
		setDifferences(doc.getDifferences());
		doc.addDifferingChangedListener(this);
		group.addValueChangeListener(this);
	}
	
	@Override
	public void detach() {
		
		doc.removeDifferingChangedListener(this);
		
		super.detach();
	}
	
	@Override
	public void differencesChanged(final Set<DocDifference> diffs) {
		getUI().access(new Runnable() {
			@Override
			public void run() {
				setDifferences(diffs);
			}
		});
	}

	private void setDifferences(Set<DocDifference> diffs) {
		group.removeAllItems();
		
		if (diffs.isEmpty()) {
			group.addItem(mineThatEqualsBase);
			group.select(mineThatEqualsBase);
			setCurrentState(mineThatEqualsBase);
			return;
		}
		
		EditorState base = new EditorState(DocType.BASE, null);
		
		DocDifference my = myDifference(diffs);
		EditorState myState;
		if (my==null) {
			myState = mineThatEqualsBase;
		}
		else {
			group.addItem(base);
			myState = new EditorState(DocType.MINE, my);
		}
		group.addItem(myState);
		
		for (DocDifference d : diffs) {
			if (!d.getUserId().equals(userId)) {
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
	
	private DocDifference myDifference(Set<DocDifference> diffs) {
		for (DocDifference d : diffs) {
			if (d.getUserId().equals(userId)) {
				return d;
			}
		}
		return null;
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
			st = mineThatEqualsBase;
		}
		setCurrentState(st);
	}
	
	
}
