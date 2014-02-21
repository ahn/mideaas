package org.vaadin.mideaas.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceEditor.DiffEvent;
import org.vaadin.aceeditor.AceEditor.DiffListener;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.aceeditor.TextRange;
import org.vaadin.aceeditor.client.AceAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.mideaas.editor.AsyncErrorChecker.ResultListener;
import org.vaadin.mideaas.editor.ErrorChecker.Error;
import org.vaadin.mideaas.editor.MultiUserEditorUserGroup.EditorStateChangedEvent;
import org.vaadin.mideaas.editor.MultiUserEditorUserGroup.EditorStateChangedListener;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@StyleSheet("ace-markers.css")
@SuppressWarnings("serial")
public class MultiUserEditor extends CustomComponent implements ResultListener, org.vaadin.mideaas.editor.SharedDoc.Listener {
		
	private final EditorUser user;
	private final MultiUserDoc mud;
	private final AceEditor editor;
	private SharedDoc activeDoc;
	
	private final HorizontalLayout hBar = new HorizontalLayout();
	private MultiUserEditorUserGroup group;
	private AsyncErrorChecker checker;
	
	private final Label titleLabel = new Label();

	public MultiUserEditor(EditorUser user, MultiUserDoc mud) {
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
		
		SharedDoc myDoc = mud.getUserDoc(user).getDoc();
		myDoc.addListener(this);
	}
	

	@Override
	public void detach() {
		super.detach();
		
		if (activeDoc!=null) {
			activeDoc.detachEditor(editor);
		}
		
		SharedDoc myDoc = mud.getUserDoc(user).getDoc();
		myDoc.removeListener(this);
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
	
	@Override
	public void errorsChecked(final List<Error> errors) {
		UI ui = getUI();
		if (ui!=null) {
			ui.access(new Runnable() {
				@Override
				public void run() {
					setEditorDoc(docWithErrors(getDoc(), errors));
				}
			});
		}
	}
	
	private void setEditorDoc(AceDoc doc) {
		boolean wasReadOnly = editor.isReadOnly();
		editor.setReadOnly(false);
		editor.setDoc(doc);
		editor.setReadOnly(wasReadOnly);
	}
	
	private AceDoc docWithErrors(AceDoc doc, List<Error> errors) {
		HashMap<String, AceMarker> markers = new HashMap<String, AceMarker>(errors.size());
		HashSet<MarkerAnnotation> manns = new HashSet<MarkerAnnotation>(errors.size());
		for (Error err : errors) {
			AceMarker m = markerFromError(newMarkerId(), err, doc.getText());
			markers.put(m.getMarkerId(), m);
			AceAnnotation ann = new AceAnnotation(err.message, AceAnnotation.Type.error);
			manns.add(new MarkerAnnotation(m.getMarkerId(), ann));
		}
		return doc.withMarkers(markers).withMarkerAnnotations(manns);		
	}
	
	private long latestMarkerId = 0L;
	private String newMarkerId() {
		// TODO ?
		return "error" + this.hashCode() + (++latestMarkerId);
	}
	
	private static AceMarker markerFromError(String markerId, Error e, String text) {
		AceRange range = new TextRange(text, e.start, e.start==e.end ? e.start+1 : e.end);
		String cssClass = "myerrormarker1";
		AceMarker.Type type = AceMarker.Type.text;
		boolean inFront = true;
		AceMarker.OnTextChange onChange = AceMarker.OnTextChange.ADJUST;
		return new AceMarker(markerId, range, cssClass, type, inFront, onChange);
	}

	@Override
	public void changed() {
		if (checker!=null) {
			checker.checkErrors(editor.getValue(), this);
		}
	}

}
