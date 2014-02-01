package org.vaadin.mideaas.editor;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.aceeditor.TextRange;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.mideaas.editor.MultiUserEditorUserGroup.EditorStateChangedEvent;
import org.vaadin.mideaas.editor.MultiUserEditorUserGroup.EditorStateChangedListener;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@StyleSheet("ace-markers.css")
@SuppressWarnings("serial")
public class MultiUserEditor extends CustomComponent {
		
	private final EditorUser user;
	private final DocManager mud;
	private final AceEditor editor;
	private SharedDoc activeDoc;
	
	private final HorizontalLayout hBar = new HorizontalLayout();
	private MultiUserEditorUserGroup group;
	private AsyncErrorChecker checker;
	
	private final Label titleLabel = new Label();

	public MultiUserEditor(EditorUser user, DocManager mud) {
		super();
		this.user = user;
		this.mud = mud;
		editor = new AceEditor();
		
		editor.setSizeFull();
		
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(hBar);
		hBar.addComponent(titleLabel);
		layout.addComponent(editor);
		layout.setExpandRatio(editor, 1);
		setCompositionRoot(layout);
	}
	
	//setAcePath("/mideaas/static/ace");
	public void setAcePath(String path) {
		editor.setThemePath(path);
		editor.setModePath(path);
		editor.setWorkerPath(path); 
	}
	
	public void setTitle(String title) {
		titleLabel.setValue(title);
	}
	
	public void setErrorChecker(AsyncErrorChecker checker) {
		this.checker = checker;
	}
	
	public void setMode(AceMode mode) {
		editor.setMode(mode);
	}
	
	public void setTheme(AceTheme theme) {
		editor.setTheme(theme);
	}
	
	public void setWordWrap(boolean b) {
		editor.setWordWrap(true);
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		editor.addSelectionChangeListener(listener);
	}

	public void addTextChangeListener(TextChangeListener listener) {
		editor.addTextChangeListener(listener);
	}

	public TextRange getSelection() {
		return editor.getSelection();
	}

	public AceDoc getDoc() {
		return editor.getDoc();
	}

	public void setSuggestionExtension(SuggestionExtension se) {
		se.extend(editor);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		group = new MultiUserEditorUserGroup(user, mud);
		setEditorState(group.getEditorState());
		hBar.addComponent(group);
		group.addDocStateChangedListener(new EditorStateChangedListener() {
			@Override
			public void stateChanged(EditorStateChangedEvent e) {
				setEditorState(e.state);
				editor.focus();
			}
		});
	
	}
	

	@Override
	public void detach() {
		super.detach();
		
		if (activeDoc!=null) {
			activeDoc.detachEditor(editor);
		}	
	}
	

	private void setEditorState(EditorState editorState) {
		
		EditorUser u = editorState.getUser();
		if (u!=null) {
			setActiveDocToUser(u);
		}
		else if (editorState.type==EditorState.DocType.BASE) {
			setActiveDocToBase();
		}
	}
	
	private void setActiveDocToBase() {
		setActiveDoc(mud.getBase());
		editor.setReadOnly(true);
	}

	
	private void setActiveDocToUser(EditorUser user) {
		setActiveDoc(mud.getUserDoc(user).getDoc());
		editor.setReadOnly(!this.user.equals(user));
	}
	
	private void setActiveDoc(SharedDoc doc) {
		if (activeDoc!=null) {
			activeDoc.detachEditor(editor);
		}
		activeDoc = doc;
		activeDoc.attachEditor(editor);
	}

}
